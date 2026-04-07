package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userStorage;

    @Override
    @Transactional
    public UserDto create(UserDto dto) {
        User entity = UserMapper.toUser(dto);
        User saved = userStorage.save(entity);
        log.info("User registered with ID: {}", saved.getId());
        return UserMapper.toUserDto(saved);
    }

    @Override
    @Transactional
    public UserDto update(Long id, UserDto dto) {
        User existingUser = userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));

        if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
            existingUser.setName(dto.getName());
        }
        if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
            existingUser.setEmail(dto.getEmail());
        }

        return UserMapper.toUserDto(userStorage.save(existingUser));
    }

    @Override
    public UserDto getById(Long id) {
        return userStorage.findById(id)
                .map(UserMapper::toUserDto)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
    }

    @Override
    public List<UserDto> findAll() {
        return userStorage.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!userStorage.existsById(id)) {
            throw new NotFoundException("Cannot delete: user not found  id: " + id);
        }
        userStorage.deleteById(id);
        log.info("User ID: {} removed", id);
    }
}