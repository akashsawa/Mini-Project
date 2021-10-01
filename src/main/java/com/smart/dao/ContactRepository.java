package com.smart.dao;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.entities.Contact;

public interface ContactRepository extends JpaRepository<Contact,Integer>
{
	//pagination method
	@Query("from Contact as c where c.user.id= :userId")
	public Page<Contact> findContactsByUser(@RequestParam("userId") int userId,Pageable pePegeable);
	//pageable obj contains current page and contacts per page
	
	//sublist of list of object is page
}
