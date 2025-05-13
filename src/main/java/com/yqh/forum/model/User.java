package com.yqh.forum.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

// @Data 注解：自动生成 getter/setter/toString/equals/hashCode 方法
// @Entity：实体类
// @Table(name = "users")：数据库表名
// 使用了JPA
@Data
@Entity
@Table(name = "users")
// 建议排除关联字段在 equals/hashCode/toString 之外，防止问题
@EqualsAndHashCode(exclude = {"roles"})
@ToString(exclude = {"roles"})
public class User {
    //@Id：主键
    @Id
    //@GeneratedValue：主键生成策略, strategy = GenerationType.IDENTITY: 主键自增
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //nullable = false: 不能为空，unique = true: 唯一性
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    //name = "avatar_url"：数据库字段名
    @Column(name = "avatar_url")
    //默认头像
    private String avatar = "/images/default-avatar.png";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // **新增：与 Role 实体的 @ManyToMany 关联映射**
    // FetchType.EAGER 表示加载 User 时立即加载关联的 Role 集合
    @ManyToMany(fetch = FetchType.EAGER)
    // @JoinTable 定义了关联表的名称和外键关系
    @JoinTable(
            name = "user_roles", // 关联表的名称 (需要与数据库中的关联表名一致)
            joinColumns = @JoinColumn(name = "user_id"), // 在 user_roles 表中指向 User 实体的外键列名 (需要与数据库列名一致)
            inverseJoinColumns = @JoinColumn(name = "role_id") // 在 user_roles 表中指向 Role 实体的外键列名 (需要与数据库列名一致)
    )
    private Set<Role> roles = new HashSet<>(); // 用户拥有的角色集合
} 