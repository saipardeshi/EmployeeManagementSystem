package com.mainapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mainapp.entity.Employee;
import com.mainapp.entity.User;
import com.mainapp.service.EmployeeService;
import com.mainapp.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
@Controller
public class SigninSignupLogout {
	@Autowired
	UserService uService;
	@Autowired
	EmployeeService eService;

	@RequestMapping (value ="/")
	public String  showSignupAndSignin() {
		return "SignupAndSignin";
		
	}
	@RequestMapping(value="/signup")
	public String handleSignup(@ModelAttribute User u,HttpServletRequest req)
	{
		String status =uService.addUser(u);
		HttpSession s=req.getSession();
		s.setAttribute("msg",status);
		
		return "redirect:/";
	}
	@RequestMapping(value="/signin")
	public String handleSignin(@RequestParam("username") String name, @RequestParam("password") String pass, HttpServletRequest req , Model model
			)
	{
			String status =uService.checkUser(name,pass);
			if(status.equals("exists"))
			{
				User u=uService.getUser(name);
				req.getSession().setAttribute("token", u);
				
				List<Employee> le=eService.readAll();
				long ec=eService.empCount();
				long dc=eService.depCount();
				long pc=eService.proCount();
				
				model.addAttribute("le",le);
				model.addAttribute("ec",ec);
				model.addAttribute("pc",pc);
				model.addAttribute("dc",dc);
				
				

				
				return "dashboard";
				
			}
			else{
				req.getSession().setAttribute("msg",status);
				return "redirect:/";
			}	
	}
	public String handleLogout(HttpServletRequest req)
	{
		req.getSession().removeAttribute("token");
		req.getSession().setAttribute("msg", "you have been logout Succesfully");
		return "SignupAndSignin";
	}
}
