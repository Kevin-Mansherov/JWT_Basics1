package com.example.jwt_basics1.service;

import com.example.jwt_basics1.dto.UserDto;
import com.example.jwt_basics1.entity.User;

import java.util.List;

public interface UserService {
    List<UserDto> getAllUsers();
    UserDto getUserById(Long id);
    UserDto createUser(UserDto userDto);
    UserDto updateUser(Long id, UserDto userDto);
    void deleteUser(Long id);
}
