package com.mgs.claudecode.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.mgs.claudecode.dto.PersonRequest;
import com.mgs.claudecode.dto.PersonResponse;
import com.mgs.claudecode.entity.Person;
import com.mgs.claudecode.exception.EmailAlreadyInUseException;
import com.mgs.claudecode.exception.PersonNotFoundException;
import com.mgs.claudecode.repository.PersonRepository;

@ExtendWith(MockitoExtension.class)
class PersonServiceTests {

	private static final LocalDate BIRTH_DATE = LocalDate.of(1990, 5, 1);

	private static final PersonRequest ALICE = new PersonRequest("Alice", "Smith", "alice@example.com", BIRTH_DATE);

	@Mock
	private PersonRepository repository;

	@InjectMocks
	private PersonService service;

	@Test
	void findAllMapsEveryPerson() {
		when(repository.findAll()).thenReturn(List.of(person(1L, "Alice", "alice@example.com"),
				person(2L, "Bob", "bob@example.com")));

		List<PersonResponse> result = service.findAll();

		assertThat(result).extracting(PersonResponse::id).containsExactly(1L, 2L);
		assertThat(result).extracting(PersonResponse::firstName).containsExactly("Alice", "Bob");
	}

	@Test
	void findAllReturnsEmptyListWhenNoPeople() {
		when(repository.findAll()).thenReturn(List.of());

		assertThat(service.findAll()).isEmpty();
	}

	@Test
	void findByIdReturnsPerson() {
		when(repository.findById(1L)).thenReturn(Optional.of(person(1L, "Alice", "alice@example.com")));

		assertThat(service.findById(1L))
			.isEqualTo(new PersonResponse(1L, "Alice", "Smith", "alice@example.com", BIRTH_DATE));
	}

	@Test
	void findByIdThrowsWhenMissing() {
		when(repository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.findById(99L)).isInstanceOf(PersonNotFoundException.class)
			.hasMessage("Person not found: 99");
	}

	@Test
	void createSavesNewPerson() {
		when(repository.existsByEmail("alice@example.com")).thenReturn(false);
		when(repository.save(any(Person.class))).thenAnswer(invocation -> {
			Person saved = invocation.getArgument(0);
			ReflectionTestUtils.setField(saved, "id", 1L);
			return saved;
		});

		PersonResponse result = service.create(ALICE);

		ArgumentCaptor<Person> captor = ArgumentCaptor.forClass(Person.class);
		verify(repository).save(captor.capture());
		assertThat(captor.getValue().getEmail()).isEqualTo("alice@example.com");
		assertThat(result).isEqualTo(new PersonResponse(1L, "Alice", "Smith", "alice@example.com", BIRTH_DATE));
	}

	@Test
	void createRejectsDuplicateEmail() {
		when(repository.existsByEmail("alice@example.com")).thenReturn(true);

		assertThatThrownBy(() -> service.create(ALICE)).isInstanceOf(EmailAlreadyInUseException.class)
			.hasMessage("Email already in use: alice@example.com");
		verify(repository, never()).save(any());
	}

	@Test
	void updateChangesExistingPerson() {
		Person existing = person(1L, "Alice", "alice@example.com");
		when(repository.findById(1L)).thenReturn(Optional.of(existing));
		when(repository.existsByEmailAndIdNot("alice.jones@example.com", 1L)).thenReturn(false);
		PersonRequest request = new PersonRequest("Alice", "Jones", "alice.jones@example.com", BIRTH_DATE);

		PersonResponse result = service.update(1L, request);

		assertThat(result).isEqualTo(new PersonResponse(1L, "Alice", "Jones", "alice.jones@example.com", BIRTH_DATE));
		assertThat(existing.getLastName()).isEqualTo("Jones");
		assertThat(existing.getEmail()).isEqualTo("alice.jones@example.com");
	}

	@Test
	void updateThrowsWhenMissing() {
		when(repository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.update(99L, ALICE)).isInstanceOf(PersonNotFoundException.class);
		verify(repository, never()).existsByEmailAndIdNot(any(), any());
	}

	@Test
	void updateRejectsEmailUsedByAnotherPerson() {
		Person existing = person(1L, "Alice", "alice@example.com");
		when(repository.findById(1L)).thenReturn(Optional.of(existing));
		when(repository.existsByEmailAndIdNot("bob@example.com", 1L)).thenReturn(true);
		PersonRequest request = new PersonRequest("Alice", "Smith", "bob@example.com", BIRTH_DATE);

		assertThatThrownBy(() -> service.update(1L, request)).isInstanceOf(EmailAlreadyInUseException.class);
		assertThat(existing.getEmail()).isEqualTo("alice@example.com");
	}

	@Test
	void deleteRemovesExistingPerson() {
		Person existing = person(1L, "Alice", "alice@example.com");
		when(repository.findById(1L)).thenReturn(Optional.of(existing));

		service.delete(1L);

		verify(repository).delete(existing);
	}

	@Test
	void deleteThrowsWhenMissing() {
		when(repository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.delete(99L)).isInstanceOf(PersonNotFoundException.class);
		verify(repository, never()).delete(any());
	}

	private static Person person(Long id, String firstName, String email) {
		Person person = new Person(firstName, "Smith", email, BIRTH_DATE);
		ReflectionTestUtils.setField(person, "id", id);
		return person;
	}

}
