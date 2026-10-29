package com.mgs.claudecode.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mgs.claudecode.dto.PersonRequest;
import com.mgs.claudecode.dto.PersonResponse;
import com.mgs.claudecode.entity.Person;
import com.mgs.claudecode.exception.EmailAlreadyInUseException;
import com.mgs.claudecode.exception.PersonNotFoundException;
import com.mgs.claudecode.repository.PersonRepository;

@Service
@Transactional(readOnly = true)
public class PersonService {

	private final PersonRepository repository;

	public PersonService(PersonRepository repository) {
		this.repository = repository;
	}

	public List<PersonResponse> findAll() {
		return repository.findAll().stream().map(PersonResponse::from).toList();
	}

	public PersonResponse findById(Long id) {
		return PersonResponse.from(getPerson(id));
	}

	@Transactional
	public PersonResponse create(PersonRequest request) {
		if (repository.existsByEmail(request.email())) {
			throw new EmailAlreadyInUseException(request.email());
		}
		Person person = new Person(request.firstName(), request.lastName(), request.email(), request.birthDate());
		return PersonResponse.from(repository.save(person));
	}

	@Transactional
	public PersonResponse update(Long id, PersonRequest request) {
		Person person = getPerson(id);
		if (repository.existsByEmailAndIdNot(request.email(), id)) {
			throw new EmailAlreadyInUseException(request.email());
		}
		person.setFirstName(request.firstName());
		person.setLastName(request.lastName());
		person.setEmail(request.email());
		person.setBirthDate(request.birthDate());
		return PersonResponse.from(person);
	}

	@Transactional
	public void delete(Long id) {
		repository.delete(getPerson(id));
	}

	private Person getPerson(Long id) {
		return repository.findById(id).orElseThrow(() -> new PersonNotFoundException(id));
	}

}
