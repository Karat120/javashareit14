package ru.practicum.shareit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class UserServiceIntegrationTest {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void create_shouldPersistUser() {
        UserDto created = userService.create(user("u1@mail.com", "User 1"));

        assertNotNull(created.getId());
        assertEquals("u1@mail.com", created.getEmail());
    }

    @Test
    void update_shouldChangeFields() {
        UserDto created = userService.create(user("u2@mail.com", "User 2"));
        UserDto patch = UserDto.builder().name("Updated").build();

        UserDto updated = userService.update(created.getId(), patch);

        assertEquals("Updated", updated.getName());
        assertEquals("u2@mail.com", updated.getEmail());
    }

    @Test
    void getById_shouldReturnStoredUser() {
        UserDto created = userService.create(user("u3@mail.com", "User 3"));

        UserDto actual = userService.getById(created.getId());

        assertEquals(created.getId(), actual.getId());
        assertEquals("User 3", actual.getName());
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        userService.create(user("u4@mail.com", "User 4"));
        userService.create(user("u5@mail.com", "User 5"));

        assertEquals(2, userService.findAll().size());
    }

    @Test
    void delete_shouldRemoveUser() {
        UserDto created = userService.create(user("u6@mail.com", "User 6"));

        userService.delete(created.getId());

        assertThrows(NotFoundException.class, () -> userService.getById(created.getId()));
    }

    private UserDto user(String email, String name) {
        return UserDto.builder()
                .email(email)
                .name(name)
                .build();
    }
}
