// package com.mainapp.controller;

// import java.util.List;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
// import org.springframework.web.bind.annotation.ModelAttribute;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RequestPart;
// import org.springframework.web.multipart.MultipartFile;

// import com.mainapp.entity.Employee;
// import com.mainapp.service.EmployeeService;

// @Controller
// public class EmployeeCrud {
// 	@Autowired
// 	EmployeeService eService;
	
// 	@RequestMapping(value="/dashboard")
// 	public String showDatabase(Model model) {
		
// 		List<Employee> le = eService.readAll();
// 		long ec = eService.empCount();
// 		long dc = eService.depCount();
// 		long pc = eService.proCount();
		
// 		model.addAttribute("le", le);
// 		model.addAttribute("ec", ec);
// 		model.addAttribute("dc", dc);
// 		model.addAttribute("pc", pc);
		
// 		return "dashboard";
// 	}
	
// 	@RequestMapping(value="/addemp")
// 	public String handleAddEmp(@ModelAttribute Employee e, @RequestPart("image") MultipartFile f) {
// 		eService.addEmployee(e, f);
// 		return "redirect:/dashboard";
// 	}
	
// 	@RequestMapping(value="/editemp")
// 	public String handleEditEmp(@RequestParam("id") int id, @RequestParam("department") String department, @RequestParam("project") String project) {
// 		eService.editEmployee(id, department, project);
// 		return "redirect:/dashboard";
// 	}
	
// 	@RequestMapping(value="/deleteemp")
// 	public String handledeleteEmp(@RequestParam("id") int id) {
// 		eService.deleteEmployee(id);
// 		return "redirect:/dashboard";
// 	}
// }
package com.mainapp.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mainapp.entity.Employee;
import com.mainapp.service.EmployeeService;

@RestController
@RequestMapping("/api/employees")
public class EmployeeCrud {

	@Autowired
	EmployeeService eService;

	// GET /api/employees            -> all employees
	// GET /api/employees?search=sai -> filter by name (case-insensitive contains)
	@GetMapping
	public List<Employee> listEmployees(@RequestParam(value = "search", required = false) String search) {
		List<Employee> all = eService.readAll();
		if (search == null || search.isBlank()) {
			return all;
		}
		String q = search.toLowerCase();
		return all.stream()
				.filter(e -> e.getName() != null && e.getName().toLowerCase().contains(q))
				.toList();
	}

	@GetMapping("/{id}")
	public ResponseEntity<Employee> getEmployee(@PathVariable int id) {
		return eService.findById(id)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@GetMapping("/stats")
	public Map<String, Long> stats() {
		return Map.of(
				"employeeCount", eService.empCount(),
				"departmentCount", eService.depCount(),
				"projectCount", eService.proCount());
	}

	// multipart/form-data: employee fields as form fields + "image" file part
	@PostMapping
	public ResponseEntity<Employee> addEmployee(@ModelAttribute Employee e,
			@RequestPart(value = "image", required = false) MultipartFile f) {
		Employee saved = eService.addEmployee(e, f);
		return ResponseEntity.status(HttpStatus.CREATED).body(saved);
	}

	// full update, JSON body
	@PutMapping("/{id}")
	public ResponseEntity<Employee> updateEmployee(@PathVariable int id, @RequestBody Employee updated) {
		return eService.updateEmployee(id, updated)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteEmployee(@PathVariable int id) {
		return eService.deleteEmployee(id)
				? ResponseEntity.noContent().build()
				: ResponseEntity.notFound().build();
	}
}