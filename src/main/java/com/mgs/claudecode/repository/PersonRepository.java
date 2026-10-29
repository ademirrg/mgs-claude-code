package com.mgs.claudecode.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mgs.claudecode.entity.Person;

public interface PersonRepository extends JpaRepository<Person, Long> {

	boolean existsByEmail(String email);

	boolean existsByEmailAndIdNot(String email, Long id);

}
