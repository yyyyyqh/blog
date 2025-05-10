package com.yqh.forum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeRequests()
                // 放行静态资源路径（优先级高）
                .antMatchers(
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/webjars/**",
                        "/uploads/avatars/**"
                ).permitAll()
                // 放行其他公开路径
                .antMatchers(
                        "/",
                        "/post",
                        "/post/search",
                        "/post/*",
                        "/user/register",
                        "/user/login"
                ).permitAll()
//                放行的路径
//                .antMatchers("/", "/post", "/post/search", "/post/*", "/user/register", "/user/login",
//                           "/css/**", "/js/**", "/images/**", "/uploads/**", "/_static/**", "/webjars/**",
//                        "https://cdn.bootcdn.net/**").permitAll()
//                其他路径需要验证
                .anyRequest().authenticated()
                .and()
            .formLogin()
                .loginPage("/user/login")
                .loginProcessingUrl("/user/login")
                .defaultSuccessUrl("/post", true)
                .failureUrl("/user/login?error")
                .permitAll()
                .and()
            .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/post")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll();
        
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
} 