package com.yqh.forum.security; // 建议放在一个与安全相关的包中

import com.yqh.forum.model.User; // 导入您的 User 实体
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
// ... 其他导入

import java.util.Collection;

public class ForumUserDetails implements UserDetails {

    // UserDetails 接口需要的字段和方法
    private String username;
    private String password;
    private boolean enabled; // 账户是否启用
    private boolean accountNonExpired; // 账户是否未过期
    private boolean accountNonLocked; // 账户是否未锁定
    private boolean credentialsNonExpired; // 凭证是否未过期
    private Collection<? extends GrantedAuthority> authorities; // 用户的权限/角色列表

    // ***从 User 实体或其他来源添加到这里的字段 (这些字段可以通过 #authentication.principal 访问)***
    private Long id;
    private String email;
    private String avatar; // <-- 这个字段和其 getter 会被 #authentication.principal.avatar 访问

    // 构造方法：通常接收您的 User 实体和其他安全相关信息来创建 UserDetails 实例
    public ForumUserDetails(User user, Collection<? extends GrantedAuthority> authorities) {
        this.username = user.getUsername();
        this.password = user.getPassword(); // 注意密码的处理，可能不需要在 UserDetails 中保留太久
        // 根据您的 User 实体或业务逻辑设置账户状态
        this.enabled = true; // 示例
        this.accountNonExpired = true; // 示例
        this.accountNonLocked = true; // 示例
        this.credentialsNonExpired = true; // 示例
        this.authorities = authorities; // 设置用户的权限/角色

        // ***填充您需要的额外字段***
        this.id = user.getId();
        this.email = user.getEmail();
        this.avatar = user.getAvatar(); // <-- 从 User 实体获取 avatar 填充到这里
    }

    // ***实现 UserDetails 接口的方法***
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override
    public String getPassword() { return password; }
    @Override
    public String getUsername() { return username; }
    @Override
    public boolean isAccountNonExpired() { return accountNonExpired; }
    @Override
    public boolean isAccountNonLocked() { return accountNonLocked; }
    @Override
    public boolean isCredentialsNonExpired() { return credentialsNonExpired; }
    @Override
    public boolean isEnabled() { return enabled; }

    // ***为需要在前端通过 #authentication.principal 访问的额外字段提供 getter 方法***
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getAvatar() { return avatar; } // <-- 这个 getAvatar() 方法是关键
}