package fr.zied.bouteraa.OAuth2OpenIDkeycloak.rest.api.service;

import java.util.List;
import java.util.Optional;

import fr.zied.bouteraa.OAuth2OpenIDkeycloak.rest.api.entity.Student;

public interface StudentService {

	 List<Student> fetchAllStudents();
	 Optional<Student> retrieveByStudentId(Integer studentId);
	 Integer integrationStudent(Student student);
}
