package com.smart.controller;


import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import com.smart.dao.UserRepository;
import com.smart.entities.User;

@Controller
public class HomeController 
{
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
//	@GetMapping("/test")
//	@ResponseBody
//	public String test()
//	{
//		User user=new User();
//		user.setName("akash");
//		user.setEmail("akash@gmail.com");
//		userRepository.save(user);
//		return "working";
//	}
	
	@RequestMapping("/")
	public String home(Model model)
	{
		model.addAttribute("title","home-smart contact manager");
		return "home";
	}
	
	@RequestMapping("/about")
	public String about(Model model)
	{
		model.addAttribute("title","home-smart contacyt manager");
		return "about";
	}
	
	@RequestMapping("/signup")
	public String signup(Model model)
	{
		model.addAttribute("user",new User());
		return "signup";
	}
	
	@RequestMapping(value="/do_register",method=RequestMethod.POST)
	public String registerUser(@ModelAttribute("user")User user,@RequestParam(value="agreement",defaultValue="false") boolean agreement,Model model,HttpSession session)
	{
		
		try 
		{
			if(!agreement)
			{
				System.out.println("not agreed terms and conditions");
				throw new Exception();
			}
			
			user.setRole("ROLE_USER");
			user.setImageUrl("def.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			System.out.println("agreement"+agreement);
			System.out.println("USER"+user);
			
			User result=this.userRepository.save(user);
			model.addAttribute("user",new User());
			session.setAttribute("message", new com.smart.helper.Message("success !", "alert-success"));
			return "signup";
		}
		catch(Exception e)
		{
			e.printStackTrace();
			model.addAttribute(e);
			session.setAttribute("message", new com.smart.helper.Message("something went wrong !"+e.getMessage(), "alert-danger"));
			return "signup";
		}
	
	}
	
	
	
}
