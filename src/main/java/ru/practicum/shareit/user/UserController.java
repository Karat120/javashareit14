package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable Long id) {
        log.info("Fetching user ID: {}", id);
        return userService.getById(id);
    }

    @GetMapping
    public List<UserDto> getUsers() {
        log.info("Fetching all users");
        return userService.findAll();
    }

    @PostMapping
    public UserDto saveUser(@Valid @RequestBody UserDto dto) {
        log.info("Creating user: {}", dto.getEmail());
        return userService.create(dto);
    }

    @PatchMapping("/{id}")
    public UserDto editUser(@PathVariable Long id, @RequestBody UserDto dto) {
        log.info("Updating user ID: {}", id);
        return userService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void removeUser(@PathVariable Long id) {
        log.info("Deleting user ID: {}", id);
        userService.delete(id);
    }
}