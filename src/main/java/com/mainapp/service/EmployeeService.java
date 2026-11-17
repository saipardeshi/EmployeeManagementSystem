package com.mainapp.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mainapp.entity.Employee;
import com.mainapp.respository.EmployeeRepository;

@Service
public class EmployeeService {

	private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif");

	@Autowired
	private EmployeeRepository eRepo;

	@Value("${app.upload-dir:uploads}")
	private String uploadDir;

	public Employee addEmployee(Employee e, MultipartFile f) {
		Employee saved = eRepo.save(e);

		if (f != null && !f.isEmpty()) {
			String extension = getSafeExtension(f.getOriginalFilename());
			String imageName = "emp_" + saved.getId() + "_" + System.currentTimeMillis() + extension;
			if (saveFile(f, imageName)) {
				saved.setImageName(imageName);
				saved = eRepo.save(saved);
			}
		}
		return saved;
	}

	private boolean saveFile(MultipartFile f, String imageName) {
		try {
			Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
			Files.createDirectories(dir);
			Path target = dir.resolve(imageName).normalize();
			if (!target.startsWith(dir)) {
				System.err.println("Security warning: target file is outside upload directory: " + imageName);
				return false;
			}
			try (InputStream is = f.getInputStream()) {
				Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
			}
			return true;
		} catch (IOException ex) {
			System.err.println("Failed to save employee photo: " + ex.getMessage());
			return false;
		}
	}

	private void deleteImageFile(String imageName) {
		if (imageName == null || imageName.isBlank()) {
			return;
		}
		try {
			Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
			Path target = dir.resolve(imageName).normalize();
			if (target.startsWith(dir)) {
				Files.deleteIfExists(target);
			}
		} catch (Exception ex) {
			System.err.println("Warning: could not delete old image file " + imageName + ": " + ex.getMessage());
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
		return eRepo.findAll().stream()
				.map(Employee::getDepartment)
				.filter(d -> d != null && !d.trim().isEmpty())
				.map(String::trim)
				.distinct()
				.count();
	}

	public long proCount() {
		return eRepo.findAll().stream()
				.map(Employee::getProject)
				.filter(p -> p != null && !p.trim().isEmpty())
				.map(String::trim)
				.distinct()
				.count();
	}

	/** Partial update for department and project. */
	public Optional<Employee> editEmployee(int id, String department, String project) {
		return eRepo.findById(id).map(e -> {
			if (department != null && !department.isBlank()) e.setDepartment(department.trim());
			if (project != null && !project.isBlank()) e.setProject(project.trim());
			return eRepo.save(e);
		});
	}

	/** Full or partial update of editable fields. */
	public Optional<Employee> updateEmployee(int id, Employee updated) {
		return eRepo.findById(id).map(e -> {
			if (updated.getName() != null && !updated.getName().isBlank()) e.setName(updated.getName().trim());
			if (updated.getDepartment() != null && !updated.getDepartment().isBlank()) e.setDepartment(updated.getDepartment().trim());
			if (updated.getProject() != null && !updated.getProject().isBlank()) e.setProject(updated.getProject().trim());
			if (updated.getDate() != null && !updated.getDate().isBlank()) e.setDate(updated.getDate().trim());
			if (updated.getLocation() != null && !updated.getLocation().isBlank()) e.setLocation(updated.getLocation().trim());
			if (updated.getContact() != null && !updated.getContact().isBlank()) e.setContact(updated.getContact().trim());
			if (updated.getImageName() != null && !updated.getImageName().isBlank()) e.setImageName(updated.getImageName().trim());
			return eRepo.save(e);
		});
	}

	public Optional<Employee> updateEmployeePhoto(int id, MultipartFile file) {
		return eRepo.findById(id).map(e -> {
			if (file != null && !file.isEmpty()) {
				String oldImage = e.getImageName();
				String extension = getSafeExtension(file.getOriginalFilename());
				String imageName = "emp_" + e.getId() + "_" + System.currentTimeMillis() + extension;
				if (saveFile(file, imageName)) {
					deleteImageFile(oldImage);
					e.setImageName(imageName);
					return eRepo.save(e);
				}
			}
			return e;
		});
	}

	public boolean deleteEmployee(int id) {
		return eRepo.findById(id).map(e -> {
			deleteImageFile(e.getImageName());
			eRepo.delete(e);
			return true;
		}).orElse(false);
	}

	private String getSafeExtension(String filename) {
		if (filename == null || !filename.contains(".")) {
			return ".jpg";
		}
		String ext = filename.substring(filename.lastIndexOf('.')).toLowerCase();
		return ALLOWED_EXTENSIONS.contains(ext) ? ext : ".jpg";
	}
}
