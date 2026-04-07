package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;
import java.util.List;

public interface UserService {
    void delete(Long id);

    List<UserDto> findAll();

    UserDto getById(Long id);

    UserDto update(Long id, UserDto userDto);

    UserDto create(UserDto userDto);
}