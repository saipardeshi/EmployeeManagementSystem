// package com.mainapp.controller;

// import java.util.List;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
// import org.springframework.web.bind.annotation.ModelAttribute;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;

// import com.mainapp.entity.Employee;
// import com.mainapp.entity.User;
// import com.mainapp.service.EmployeeService;
// import com.mainapp.service.UserService;

// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpSession;
// @Controller
// public class SigninSignupLogout {
// 	@Autowired
// 	UserService uService;
// 	@Autowired
// 	EmployeeService eService;

// 	@RequestMapping (value ="/")
// 	public String  showSignupAndSignin() {
// 		return "SignupAndSignin";
		
// 	}
// 	@RequestMapping(value="/signup")
// 	public String handleSignup(@ModelAttribute User u,HttpServletRequest req)
// 	{
// 		String status =uService.addUser(u);
// 		HttpSession s=req.getSession();
// 		s.setAttribute("msg",status);
		
// 		return "redirect:/";
// 	}
// 	@RequestMapping(value="/signin")
// 	public String handleSignin(@RequestParam("username") String name, @RequestParam("password") String pass, HttpServletRequest req , Model model
// 			)
// 	{
// 			String status =uService.checkUser(name,pass);
// 			if(status.equals("exists"))
// 			{
// 				User u=uService.getUser(name);
// 				req.getSession().setAttribute("token", u);
				
// 				List<Employee> le=eService.readAll();
// 				long ec=eService.empCount();
// 				long dc=eService.depCount();
// 				long pc=eService.proCount();
				
// 				model.addAttribute("le",le);
// 				model.addAttribute("ec",ec);
// 				model.addAttribute("pc",pc);
// 				model.addAttribute("dc",dc);
				
				

				
// 				return "dashboard";
				
// 			}
// 			else{
// 				req.getSession().setAttribute("msg",status);
// 				return "redirect:/";
// 			}	
// 	}
// 	public String handleLogout(HttpServletRequest req)
// 	{
// 		req.getSession().removeAttribute("token");
// 		req.getSession().setAttribute("msg", "you have been logout Succesfully");
// 		return "SignupAndSignin";
// 	}
// }
package com.mainapp.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mainapp.entity.User;
import com.mainapp.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
public class SigninSignupLogout {

	@Autowired
	UserService uService;

	@PostMapping("/signup")
	public ResponseEntity<Map<String, String>> signup(@RequestBody User u) {
		boolean created = uService.addUser(u);
		if (created) {
			return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Account created successfully"));
		}
		return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Username already taken"));
	}

	@PostMapping("/signin")
	public ResponseEntity<?> signin(@RequestBody User credentials, HttpServletRequest req) {
		return uService.authenticate(credentials.getUsername(), credentials.getPassword())
				.map(user -> {
					HttpSession session = req.getSession();
					session.setAttribute("username", user.getUsername());
					return ResponseEntity.ok(Map.of("username", user.getUsername(), "email", user.getEmail()));
				})
				.orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("message", "Invalid username or password")));
	}

	@PostMapping("/logout")
	public ResponseEntity<Map<String, String>> logout(HttpServletRequest req) {
		HttpSession session = req.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
	}

	// lets the frontend check "am I still logged in?" on page load/refresh
	@GetMapping("/me")
	public ResponseEntity<?> me(HttpServletRequest req) {
		HttpSession session = req.getSession(false);
		if (session != null && session.getAttribute("username") != null) {
			return ResponseEntity.ok(Map.of("username", session.getAttribute("username")));
		}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not signed in"));
	}
}