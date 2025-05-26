package com.yqh.forum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            //  允许同源嵌入（嵌入iframe）
            .headers(headers -> headers
                    .frameOptions(
                            //在 Java 8 及更高版本中，如果一个 Lambda 表达式仅仅是调用一个现有方法，并且参数列表匹配，那么它可以被更简洁的方法引用 (Method Reference) 替换。
                            HeadersConfigurer.FrameOptionsConfig::sameOrigin
                            //frameOptions -> frameOptions
                            //        .sameOrigin() // 允许同源嵌入
                            // .deny() // 原始设置，拒绝任何来源嵌入
                            // .disable() // 完全禁用 (不推荐，有安全风险)
                    )
            )
            .csrf().disable()
            .authorizeRequests()
                // 放行静态资源路径（优先级高）
                .antMatchers(
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/webjars/**",
                        "/uploads/avatars/**",
                        "/uploads/post-images/**"
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
    // 使用BCrypt加密密码
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // 使用BCrypt加密密码
    }
} 