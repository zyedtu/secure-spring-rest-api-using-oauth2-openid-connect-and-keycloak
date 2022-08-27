package fr.zied.bouteraa.OAuth2OpenIDkeycloak.rest.api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import fr.zied.bouteraa.OAuth2OpenIDkeycloak.rest.api.dao.StudentJpaRepository;
import fr.zied.bouteraa.OAuth2OpenIDkeycloak.rest.api.entity.Student;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {
	
	private final StudentJpaRepository studentJpaRepository;

	@Override
	public List<Student> fetchAllStudents() {
		return studentJpaRepository.findAll();
	}

	@Override
	public Optional<Student> retrieveByStudentId(Integer studentId) {
		return studentJpaRepository.findById(studentId);
	}

	@Override
	public Integer integrationStudent(Student student) {
		Student save = studentJpaRepository.save(student);
		return save.getId();
	}

}
