package fr.zied.bouteraa.OAuth2OpenIDkeycloak.rest.api.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.zied.bouteraa.OAuth2OpenIDkeycloak.rest.api.entity.Student;
import fr.zied.bouteraa.OAuth2OpenIDkeycloak.rest.api.service.StudentService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class StudentResource {

	private final StudentService studentService;
	
	@GetMapping(value = "/v1/students")
	public ResponseEntity<List<Student>> getAllStudent() {
		List<Student> fetchAllStudents = studentService.fetchAllStudents();
		return new ResponseEntity<List<Student>>(fetchAllStudents, HttpStatus.OK);
	}
}
