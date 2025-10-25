package com.example.jwt_basics1.service;

import com.example.jwt_basics1.dto.UserDto;
import com.example.jwt_basics1.entity.Role;
import com.example.jwt_basics1.entity.User;
import com.example.jwt_basics1.repository.RoleRepository;
import com.example.jwt_basics1.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService{
    @Autowired
    private final UserRepository userRepository;
    private RoleRepository roleRepository;

    private UserDto convertToDto(User user){
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setPassword(user.getPassword());

        if(user.getRoles() != null){
            Set<String> roles = user.getRoles().stream()
                    .map(role -> role.getRoleName())
                    .collect(Collectors.toSet());

            userDto.setRoles(roles);
        }else{
            userDto.setRoles(new HashSet<>());
        }
        return userDto;
    }

    public User convertToEntity(UserDto userDto){
        User user = new User();
        user.setId(userDto.getId());
        user.setUsername(userDto.getUsername());
        user.setPassword(userDto.getPassword());

        if(user.getRoles() != null){
            List<Role> roles = userDto.getRoles().stream()
                    .map(roleName -> roleRepository.findByRoleName(roleName)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName)))
                    .toList();

            user.setRoles(roles);
        }else{
            user.setRoles(List.of());
        }
        return user;
    }


    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(this::convertToDto).orElse(null);
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = convertToEntity(userDto);
        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        Optional<User> existing = userRepository.findById(id);
        if(existing.isPresent()){
            User user = convertToEntity(userDto);
            user.setId(id);
            User updatedUser = userRepository.save(user);
            return convertToDto(updatedUser);
        }
        return null;
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
