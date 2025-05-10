package com.yqh.forum.security;

import com.yqh.forum.model.User;
import com.yqh.forum.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

//这个类实现了UserDetailsService这个接口，这个接口是Spring Security提供的，用于从数据库中查询用户信息。
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        System.out.println("Fetched User Entity Type: " + user.getClass().getName()); // 打印实体类型

        ForumUserDetails forumUserDetails = new ForumUserDetails(user, Collections.emptyList()); // 创建自定义 UserDetails
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