package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;

	@ModelAttribute
	public void addCommonData(Model m, Principal principal) {
		String userName = principal.getName();
		System.out.println("username:" + userName);
		// get user using usernme

		User user = userRepository.getUserByUserName(userName);
		System.out.println("user:" + user);

		m.addAttribute("user", user);

	}

	// home dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {

		return "user_dashboard";
	}

	// open add form handler
	@GetMapping("/add-turf")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "add turf");
		model.addAttribute("turf", new Contact());

		return "normal/add_turf";
	}

	// processing adfd contact form

	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session) {
		try {
			String name = principal.getName(); // logged in username of user
			User user = this.userRepository.getUserByUserName(name);

//			user.getContacts().add(contact);
//			contact.setUser(user);

			// processing and uploading file
			if (file.isEmpty()) {
				// if file is empty
				System.out.println("file is empty");
			} else {
				// file to folder and update name to contact
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				System.out.println("image uploaded");
			}

			user.getContacts().add(contact);
			contact.setUser(user);

			this.userRepository.save(user);

			System.out.println("DATA: " + contact);
			System.out.println("added to database");

			// success msg
			session.setAttribute("message", new Message("your turf is added !! add more...", "success"));

		} catch (Exception e) {
			System.out.println("ERROR:" + e.getMessage());

			e.printStackTrace();
			session.setAttribute("message", new Message("something went wrong", "danger"));

		}
		return "normal/add_turf";
	}

	// show contacts
	// per page 5 contents
	// current page=0
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model m, Principal principal) {
		m.addAttribute("title", "show user context");

		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);

		PageRequest pageable = PageRequest.of(page, 5);

		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", contacts.getTotalPages());

		return "normal/show_contacts";

	}

	@RequestMapping("/{cid}/contact")
	public String showContactDetails(@PathVariable("cid") Integer cid, Model model) {
		System.out.println("cid" + cid);
		Optional<Contact> contactOptional = this.contactRepository.findById(cid);
		Contact contact = contactOptional.get();

		model.addAttribute("contact", contact);
		return "normal/contact_detail";
	}

	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cid, Model model, HttpSession session) {
		Contact contact = this.contactRepository.findById(cid).get();

		System.out.println("contact " + contact.getCid());

		contact.setUser(null);

		this.contactRepository.delete(contact);
		session.setAttribute("message", new Message("contact deleted successfully !", "success"));

		return "redirect:/user/show-contacts/0";
	}

	// update form handler

	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid, Model m) {
		m.addAttribute("title", "update contact");

		Contact contact = this.contactRepository.findById(cid).get();

		m.addAttribute("contact", contact);
		return "normal/update_form";
	}

	@RequestMapping(value = "/process-update", method = RequestMethod.POST)

	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Model m, HttpSession session, Principal principal) {
		try {
			// old contact detail
			Contact oldContactDetail = this.contactRepository.findById(contact.getCid()).get();

			if (!file.isEmpty()) {
				// delete old photo
				File deleteFile = new ClassPathResource("static").getFile();
				File file1=new File(deleteFile,oldContactDetail.getImage());
				file1.delete();
				
				
				
				// update new photo

				File saveFile = new ClassPathResource("static").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			}

			else {
				contact.setImage(oldContactDetail.getImage());
			}

			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("your contact is updated...", "success"));	
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("name:" + contact.getName());
		//return "redirect:/user/"+contact.getCid()+"/contact";
		return "normal/update_form";
	}

}
