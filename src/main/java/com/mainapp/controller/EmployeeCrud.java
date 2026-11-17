package com.mainapp.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
	private EmployeeService eService;

	// GET /api/employees            -> all employees
	// GET /api/employees?search=xxx -> filter across name, department, project, location, contact
	@GetMapping
	public List<Employee> listEmployees(@RequestParam(value = "search", required = false) String search) {
		List<Employee> all = eService.readAll();
		if (search == null || search.isBlank()) {
			return all;
		}
		String q = search.trim().toLowerCase();
		return all.stream()
				.filter(e -> (e.getName() != null && e.getName().toLowerCase().contains(q))
						|| (e.getDepartment() != null && e.getDepartment().toLowerCase().contains(q))
						|| (e.getProject() != null && e.getProject().toLowerCase().contains(q))
						|| (e.getLocation() != null && e.getLocation().toLowerCase().contains(q))
						|| (e.getContact() != null && e.getContact().toLowerCase().contains(q)))
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

	// Multipart/form-data: employee fields as form fields + optional "image" file
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> addEmployeeMultipart(@ModelAttribute Employee e,
			@RequestPart(value = "image", required = false) MultipartFile f) {
		if (e.getName() == null || e.getName().trim().isEmpty()) {
			return ResponseEntity.badRequest().body(Map.of("message", "Employee name is required"));
		}
		if (e.getDepartment() == null || e.getDepartment().trim().isEmpty()) {
			return ResponseEntity.badRequest().body(Map.of("message", "Department is required"));
		}
		Employee saved = eService.addEmployee(e, f);
		return ResponseEntity.status(HttpStatus.CREATED).body(saved);
	}

	// JSON body creation without image
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> addEmployeeJson(@RequestBody Employee e) {
		if (e == null || e.getName() == null || e.getName().trim().isEmpty()) {
			return ResponseEntity.badRequest().body(Map.of("message", "Employee name is required"));
		}
		if (e.getDepartment() == null || e.getDepartment().trim().isEmpty()) {
			return ResponseEntity.badRequest().body(Map.of("message", "Department is required"));
		}
		Employee saved = eService.addEmployee(e, null);
		return ResponseEntity.status(HttpStatus.CREATED).body(saved);
	}

	// Update employee details (JSON body)
	@PutMapping("/{id}")
	public ResponseEntity<?> updateEmployee(@PathVariable int id, @RequestBody Employee updated) {
		if (updated == null) {
			return ResponseEntity.badRequest().body(Map.of("message", "Request body cannot be empty"));
		}
		return eService.updateEmployee(id, updated)
				.<ResponseEntity<?>>map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Employee not found")));
	}

	// Dedicated photo upload endpoint
	@PostMapping("/{id}/photo")
	public ResponseEntity<?> uploadPhoto(@PathVariable int id,
			@RequestPart(value = "image", required = false) MultipartFile file) {
		if (file == null || file.isEmpty()) {
			return ResponseEntity.badRequest().body(Map.of("message", "Please select a valid image file"));
		}
		return eService.updateEmployeePhoto(id, file)
				.<ResponseEntity<?>>map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Employee not found")));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteEmployee(@PathVariable int id) {
		return eService.deleteEmployee(id)
				? ResponseEntity.noContent().build()
				: ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Employee not found"));
	}
}
