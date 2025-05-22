package com.yqh.forum.security;

import com.yqh.forum.model.Role;
import com.yqh.forum.model.User;
import com.yqh.forum.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 自定义的UserDetailsService实现类，用于从数据库中加载用户信息。
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    //根据用户名从数据库加载用户信息。
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        // --- 修改部分开始 ---

        // 1. 获取用户关联的角色集合
        Set<Role> roles = user.getRoles();

        // 2. 将 Role 集合转换为 Spring Security 的 GrantedAuthority 集合
        Collection<? extends GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName())) // 将 Role 的 name 转换为 SimpleGrantedAuthority
                .collect(Collectors.toSet()); // 收集到 Set 中

        // --- 修改部分结束 ---



        System.out.println("Fetched User Entity Type: " + user.getClass().getName()); // 打印实体类型
        System.out.println("User Authorities: " + authorities); // 打印获取到的权限

        //Collections.emptyList()
        ForumUserDetails forumUserDetails = new ForumUserDetails(user, authorities); // 创建自定义 UserDetails
        System.out.println("Created UserDetails Type: " + forumUserDetails.getClass().getName()); // 打印自定义 UserDetails 类型

        return forumUserDetails; // 返回自定义 UserDetails
    }

//    private final UserRepository userRepository;
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
//
//        return org.springframework.security.core.userdetails.User
//                .withUsername(user.getUsername())
//                .password(user.getPassword())
//                .roles("USER")
//                .build();
//    }
}