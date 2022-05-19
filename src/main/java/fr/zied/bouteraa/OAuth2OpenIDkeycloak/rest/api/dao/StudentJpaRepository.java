package fr.zied.bouteraa.OAuth2OpenIDkeycloak.rest.api.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.zied.bouteraa.OAuth2OpenIDkeycloak.rest.api.entity.Student;

public interface StudentJpaRepository extends JpaRepository<Student, Integer> {

}
