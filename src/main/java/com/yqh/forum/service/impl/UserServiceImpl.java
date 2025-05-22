package com.yqh.forum.service.impl;

import com.yqh.forum.dto.UserDTO;
import com.yqh.forum.dto.UserRegistrationDTO;
import com.yqh.forum.model.User;
import com.yqh.forum.repository.UserRepository;
import com.yqh.forum.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor // Lombok 注解，自动生成构造函数
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    //注册新用户
    @Override
    @Transactional
    public UserDTO registerNewUser(UserRegistrationDTO registrationDTO) {
        if (userRepository.existsByUsername(registrationDTO.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        if (!registrationDTO.getPassword().equals(registrationDTO.getConfirmPassword())) {
            throw new RuntimeException("两次输入的密码不一致");
        }

        User user = new User();
        user.setUsername(registrationDTO.getUsername());
        user.setEmail(registrationDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));

        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    @Override
    public UserDTO findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Override
    public UserDTO findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Override
    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("当前用户不存在"));
    }

    @Override
    public void updateUser(User user) {
        userRepository.save(user);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    //更改密码
    @Override
    @Transactional
    public void changePassword(String currentPassword, String newPassword, String confirmPassword) {
        User user = getCurrentUser();
        
        // 验证当前密码
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("当前密码错误");
        }
        
        // 验证新密码
        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("两次输入的新密码不一致");
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setAvatar(user.getAvatar());
        return dto;
    }

    @Override // 确保方法签名与 UserService 接口中的一致
    public List<User> getAllUsers() {
        // 调用 UserRepository 的方法查询所有用户
        List<User> users = userRepository.findAll(); // Spring Data JPA 的 findAll() 方法通常返回 List，即使没有数据也是空 List，而不是 null


        //return users != null ? users : Collections.emptyList();

        // 或者更简洁，因为 findAll() 几乎总是返回非 null 的 List：
         return userRepository.findAll();
    }

    @Override
    @Transactional // 标记此方法为事务性的，确保数据库操作的原子性
    public String resetUserPassword(Long userId) {
        // 1. 根据用户 ID 查找用户
        Optional<User> userOptional = userRepository.findById(userId);

        User user = userOptional.orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));

        // 2. 生成一个新的随机密码
        // 这里使用 UUID 生成一个简单的随机字符串作为初始密码示例
        // 在生产环境中，请确保生成的密码足够复杂和安全
        //String newRawPassword = UUID.randomUUID().toString().substring(0, 10); // 例如生成一个10个字符的密码

        String newRawPassword = "000000";

        // 3. 编码新密码
        String encodedPassword = passwordEncoder.encode(newRawPassword);

        // 4. 更新用户实体中的密码
        user.setPassword(encodedPassword);

        // 5. 保存更新后的用户实体到数据库
        userRepository.save(user); // 这会将用户的密码更新到数据库

        // 6. **重要：将新生成的裸密码通过安全方式通知用户（通常是发送邮件）**
        // **这部分逻辑需要您根据您的邮件发送配置和实现来完成。**
        // 您需要配置 JavaMailSender，并编写发送邮件的代码。
        // 例如：
        // try {
        //     sendPasswordResetEmail(user.getEmail(), newRawPassword); // 调用发送邮件的方法
        // } catch (Exception e) {
        //     // 处理邮件发送失败的情况 - 记录日志或抛出自定义异常
        //     System.err.println("Failed to send password reset email to " + user.getEmail() + ": " + e.getMessage());
        //     // **根据您的业务需求决定是否回滚事务或标记重置失败**
        //     // throw new RuntimeException("密码重置成功，但邮件发送失败。", e); // 示例：如果邮件发送是必须的，可以抛异常
        // }


        // **返回新生成的裸密码**
        // 注意：直接在日志或消息中显示裸密码不安全，通常只在 Service 内部使用并立即发送给用户。
        // 如果邮件发送成功且 Service 不需要将密码返回给 Controller，可以将此方法的返回类型改为 void。
        return newRawPassword;
    }
} 