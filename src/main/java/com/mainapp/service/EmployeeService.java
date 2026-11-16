
// package com.mainapp.service;

// import java.io.FileOutputStream;
// import java.io.InputStream;
// import java.util.List;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;
// import org.springframework.web.multipart.MultipartFile;

// import com.mainapp.entity.Employee;
// import com.mainapp.respository.EmployeeRepository;

// @Service
// public class EmployeeService {
// 	@Autowired
// 	EmployeeRepository eRepo;
	
// 	public void addEmployee(Employee e, MultipartFile f) {
// 		Employee newEmp = eRepo.save(e);
// 		String imageName = e.getName()+""+newEmp.getId()+".jpg";
// 		newEmp.setImageName(imageName);
// 		eRepo.save(newEmp);
		
// 		try {
// 			InputStream is = f.getInputStream();
// 			byte[] b = is.readAllBytes();
			
// 			String path = "D:\\\\SEED\\\\SpringBoot\\\\demo-7\\\\src\\\\main\\\\webapp\\\\images\\\\"+imageName;
// 			FileOutputStream fos = new FileOutputStream(path);
// 			fos.write(b);
			
// 			is.close();
// 			fos.close();
			
			
// 		} catch (Exception e1) {
// 			System.out.println(e1);
// 		}
		
// 	}
	
// 	public List<Employee> readAll(){
// 		return eRepo.findAll();
// 	}
	
// 	public long empCount() {
// 		return eRepo.count();
// 	}
	
// 	public long depCount() {
// 		return eRepo.findAll().stream().map(e -> e.getDepartment()).distinct().count();
// 	}
	
// 	public long proCount() {
// 		return eRepo.findAll().stream().map(e -> e.getProject()).distinct().count();
// 	}
	
	
// 	public void editEmployee(int id, String department, String project) {
// 		Employee e = eRepo.findById(id).get();
// 		e.setDepartment(department);
// 		e.setProject(project);
// 		eRepo.save(e);
// 	}
	
// 	public void deleteEmployee(int id) {
// 		eRepo.deleteById(id);
// 	}
	
	
	
// }

package com.mainapp.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mainapp.entity.Employee;
import com.mainapp.respository.EmployeeRepository;

@Service
public class EmployeeService {

	@Autowired
	EmployeeRepository eRepo;

	@Value("${app.upload-dir}")
	private String uploadDir;

	public Employee addEmployee(Employee e, MultipartFile f) {
		Employee saved = eRepo.save(e);

		if (f != null && !f.isEmpty()) {
			String extension = getExtension(f.getOriginalFilename());
			String imageName = "emp_" + saved.getId() + extension;
			saveFile(f, imageName);
			saved.setImageName(imageName);
			saved = eRepo.save(saved);
		}
		return saved;
	}

	private void saveFile(MultipartFile f, String imageName) {
		try {
			Path dir = Paths.get(uploadDir);
			Files.createDirectories(dir);
			try (InputStream is = f.getInputStream()) {
				Files.copy(is, dir.resolve(imageName), StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException ex) {
			System.out.println("Failed to save employee photo: " + ex.getMessage());
		}
	}

	public List<Employee> readAll() {
		return eRepo.findAll();
	}

	public Optional<Employee> findById(int id) {
		return eRepo.findById(id);
	}

	public long empCount() {
		return eRepo.count();
	}

	public long depCount() {
		return eRepo.findAll().stream().map(Employee::getDepartment).distinct().count();
	}

	public long proCount() {
		return eRepo.findAll().stream().map(Employee::getProject).distinct().count();
	}

	/** Partial update, kept for backwards compatibility with the old form. */
	public Optional<Employee> editEmployee(int id, String department, String project) {
		return eRepo.findById(id).map(e -> {
			e.setDepartment(department);
			e.setProject(project);
			return eRepo.save(e);
		});
	}

	/** Full or partial update of editable fields. */
	public Optional<Employee> updateEmployee(int id, Employee updated) {
		return eRepo.findById(id).map(e -> {
			if (updated.getName() != null && !updated.getName().isBlank()) e.setName(updated.getName());
			if (updated.getDepartment() != null && !updated.getDepartment().isBlank()) e.setDepartment(updated.getDepartment());
			if (updated.getProject() != null && !updated.getProject().isBlank()) e.setProject(updated.getProject());
			if (updated.getDate() != null && !updated.getDate().isBlank()) e.setDate(updated.getDate());
			if (updated.getLocation() != null && !updated.getLocation().isBlank()) e.setLocation(updated.getLocation());
			if (updated.getContact() != null && !updated.getContact().isBlank()) e.setContact(updated.getContact());
			if (updated.getImageName() != null && !updated.getImageName().isBlank()) e.setImageName(updated.getImageName());
			return eRepo.save(e);
		});
	}

	public Optional<Employee> updateEmployeePhoto(int id, MultipartFile file) {
		return eRepo.findById(id).map(e -> {
			if (file != null && !file.isEmpty()) {
				String extension = getExtension(file.getOriginalFilename());
				String imageName = "emp_" + e.getId() + "_" + System.currentTimeMillis() + extension;
				saveFile(file, imageName);
				e.setImageName(imageName);
				return eRepo.save(e);
			}
			return e;
		});
	}

	public boolean deleteEmployee(int id) {
		if (!eRepo.existsById(id)) {
			return false;
		}
		eRepo.deleteById(id);
		return true;
	}

	private String getExtension(String filename) {
		if (filename == null || !filename.contains(".")) {
			return ".jpg";
		}
		return filename.substring(filename.lastIndexOf('.'));
	}
}