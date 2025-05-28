package com.yqh.forum.service.impl;

import com.yqh.forum.dto.UserDTO;
import com.yqh.forum.dto.UserRegistrationDTO;
import com.yqh.forum.model.User;
import com.yqh.forum.repository.UserRepository;
import com.yqh.forum.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor // Lombok 注解，自动生成构造函数
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);


    //注册新用户
    @Override
    @Transactional
    public UserDTO registerNewUser(UserRegistrationDTO registrationDTO) {
        if (userRepository.existsByUsername(registrationDTO.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        //抛出异常
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

        // 2. 生成一个新的随机密码（6位数字密码）
        SecureRandom random = new SecureRandom();
        int randomNumber = random.nextInt(1000000);
        // 使用 String.format 来确保数字是6位数，不足6位时在前面补0
        String newRawPassword = String.format("%06d", randomNumber);

        //String newRawPassword = "000000";

        // 3. 编码新密码
        String encodedPassword = passwordEncoder.encode(newRawPassword);

        // 4. 更新用户实体中的密码
        user.setPassword(encodedPassword);

        // 5. 保存更新后的用户实体到数据库（将用户的密码更新到数据库）
        userRepository.save(user);

        // **返回新生成的裸密码**
        return newRawPassword;
    }

    /**
     * 根据用户 ID 查找用户
     * @param userId 用户ID
     * @return 用户实体
     */
    @Override
    public User findById(Long userId) {
        // 1. 根据用户 ID 查找用户
        Optional<User> userOptional = userRepository.findById(userId);

        User user = userOptional.orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));

        return user;
    }

    @Override
    public void deleteUserById(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null.");
        }
        // 1. 检查用户是否存在（可选，因为 userRepository.deleteById 如果ID不存在也不会报错）
        // 但为了能抛出我们自定义的 UserNotFoundException，进行检查是好的。
        if (!userRepository.existsById(userId)) {
            logger.warn("Attempted to delete non-existent user with ID: {}", userId);
        }

        // 2. 执行删除操作
        try {
            userRepository.deleteById(userId);
            logger.info("用户 (ID: {}) 已成功删除。", userId);
        } catch (DataIntegrityViolationException e) { // 显式捕获 DataIntegrityViolationException
            logger.warn("删除用户 (ID: {}) 失败，存在数据完整性约束: {}", userId, e.getMessage());
            // 直接重新抛出，让 Controller 层的特定 catch 块来处理它，或者包装成一个自定义的业务异常
            throw e; // 或者 throw new YourBusinessRuleException("该用户尚有关联数据，无法删除。", e);
        } catch (Exception e) { // 捕获其他所有预料之外的异常
            logger.error("删除用户 (ID: {}) 时发生未知错误: {}", userId, e.getMessage(), e);
            // 对于其他未知错误，可以抛出通用运行时异常
            throw new RuntimeException("删除用户 (ID: " + userId + ") 时发生了一个未知错误。", e);
        }
    }
}