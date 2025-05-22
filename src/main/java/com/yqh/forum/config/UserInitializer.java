package com.yqh.forum.config; // 建议放在 config 包下，或者单独的 init/bootstrap 包

import com.yqh.forum.model.Role; // 确保导入 Role 类
import com.yqh.forum.model.User;
import com.yqh.forum.repository.RoleRepository; // 可能需要这个来获取或创建 Role
import com.yqh.forum.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component // 确保 Spring 能够发现并注册这个 Bean
@RequiredArgsConstructor // Lombok 注解，为 final 字段生成构造函数，用于依赖注入
public class UserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository; // 注入 RoleRepository，用于管理角色
    private final PasswordEncoder passwordEncoder; // 注入 PasswordEncoder

    @Override
    public void run(String... args) throws Exception {
        // 检查数据库中是否已存在 'root' 用户
        if (userRepository.findByUsername("root").isEmpty()) {
            System.out.println("正在创建默认用户 'root'...");

            // 1. 创建或获取 'ROLE_ADMIN' 角色
            // 检查数据库中是否已存在 'ROLE_ADMIN' 角色，如果没有则创建
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseGet(() -> {
                        Role newRole = new Role();
                        newRole.setName("ROLE_ADMIN");
                        return roleRepository.save(newRole);
                    });

            // 2. 创建用户实体
            User rootUser = new User();
            rootUser.setUsername("root");
            // 使用 passwordEncoder 加密密码 "root"
            rootUser.setPassword(passwordEncoder.encode("root"));
            rootUser.setEmail("root@example.com"); // 设置一个默认邮箱
            rootUser.setAvatar("/images/default-avatar.png"); // 设置默认头像或其他值
            rootUser.setCreatedAt(LocalDateTime.now());
            rootUser.setUpdatedAt(LocalDateTime.now());
            //rootUser.setEnabled(true); // 启用账户

            // 3. 给用户分配 'ROLE_ADMIN' 角色
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            rootUser.setRoles(roles);

            // 4. 保存用户到数据库
            userRepository.save(rootUser);
            System.out.println("默认用户 'root' (密码: root, 角色: ROLE_ADMIN) 已成功创建！");
        } else {
            System.out.println("用户 'root' 已存在，跳过创建。");
        }
    }
}