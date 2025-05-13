package com.yqh.forum.service;

import com.yqh.forum.dto.UserDTO;
import com.yqh.forum.dto.UserRegistrationDTO;
import com.yqh.forum.model.User;

import java.util.List;

public interface UserService {
    UserDTO registerNewUser(UserRegistrationDTO registrationDTO);
    UserDTO findByUsername(String username);
    UserDTO findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    User getCurrentUser();
    void updateUser(User user);
    void changePassword(String currentPassword, String newPassword, String confirmPassword);
    List<User> getAllUsers();
} 