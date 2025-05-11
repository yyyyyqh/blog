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
                //禁用Refused to display 'http://localhost:8080/' in a frame because it set 'X-Frame-Options' to 'deny'.
            .headers(headers -> headers
                    .frameOptions(frameOptions -> frameOptions
                                    .sameOrigin() // **修改：允许同源嵌入**
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