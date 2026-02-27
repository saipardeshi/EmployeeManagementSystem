package com.mainapp.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import com.mainapp.entity.User;
import com.mainapp.respository.UserRespository;

@Service
public class UserService {
	@Autowired 
    UserRespository uRepo;
	
	public String addUser(User u)
	{
		Optional<User> op=uRepo.findById(u.getUsername());
		if(op.isPresent()) {
			return "Account creaction failed";
		}
		else 
		{
			uRepo.save(u);
			return "Account creaction successful";
		}
	}
	public String checkUser(String name,String pass)
	{
		User u=uRepo.findByUsernameAndPassword(name, pass);
		if(u ==null)
		{
			return "invalid credentials";
		}
		else
		{
			return "exists";
		}
	}
	public User getUser(String name)
	{
		return uRepo.findById(name).get();
	}
}
