
package com.mainapp.service;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mainapp.entity.Employee;
import com.mainapp.respository.EmployeeRepository;

@Service
public class EmployeeService {
	@Autowired
	EmployeeRepository eRepo;
	
	public void addEmployee(Employee e, MultipartFile f) {
		Employee newEmp = eRepo.save(e);
		String imageName = e.getName()+""+newEmp.getId()+".jpg";
		newEmp.setImageName(imageName);
		eRepo.save(newEmp);
		
		try {
			InputStream is = f.getInputStream();
			byte[] b = is.readAllBytes();
			
			String path = "D:\\\\SEED\\\\SpringBoot\\\\demo-7\\\\src\\\\main\\\\webapp\\\\images\\\\"+imageName;
			FileOutputStream fos = new FileOutputStream(path);
			fos.write(b);
			
			is.close();
			fos.close();
			
			
		} catch (Exception e1) {
			System.out.println(e1);
		}
		
	}
	
	public List<Employee> readAll(){
		return eRepo.findAll();
	}
	
	public long empCount() {
		return eRepo.count();
	}
	
	public long depCount() {
		return eRepo.findAll().stream().map(e -> e.getDepartment()).distinct().count();
	}
	
	public long proCount() {
		return eRepo.findAll().stream().map(e -> e.getProject()).distinct().count();
	}
	
	
	public void editEmployee(int id, String department, String project) {
		Employee e = eRepo.findById(id).get();
		e.setDepartment(department);
		e.setProject(project);
		eRepo.save(e);
	}
	
	public void deleteEmployee(int id) {
		eRepo.deleteById(id);
	}
	
	
	
}

