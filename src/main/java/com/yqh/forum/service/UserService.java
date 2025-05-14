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
    // 重置用户密码的方法
    // 返回 String (新生成的裸密码) 或 void (如果邮件发送等通知逻辑在 Service 内部完成)
    String resetUserPassword(Long userId);
} 