package com.mainapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.mainapp.entity.Employee;
import com.mainapp.service.EmployeeService;

@Controller
public class EmployeeCrud {
	@Autowired
	EmployeeService eService;
	
	@RequestMapping(value="/dashboard")
	public String showDatabase(Model model) {
		
		List<Employee> le = eService.readAll();
		long ec = eService.empCount();
		long dc = eService.depCount();
		long pc = eService.proCount();
		
		model.addAttribute("le", le);
		model.addAttribute("ec", ec);
		model.addAttribute("dc", dc);
		model.addAttribute("pc", pc);
		
		return "dashboard";
	}
	
	@RequestMapping(value="/addemp")
	public String handleAddEmp(@ModelAttribute Employee e, @RequestPart("image") MultipartFile f) {
		eService.addEmployee(e, f);
		return "redirect:/dashboard";
	}
	
	@RequestMapping(value="/editemp")
	public String handleEditEmp(@RequestParam("id") int id, @RequestParam("department") String department, @RequestParam("project") String project) {
		eService.editEmployee(id, department, project);
		return "redirect:/dashboard";
	}
	
	@RequestMapping(value="/deleteemp")
	public String handledeleteEmp(@RequestParam("id") int id) {
		eService.deleteEmployee(id);
		return "redirect:/dashboard";
	}
}