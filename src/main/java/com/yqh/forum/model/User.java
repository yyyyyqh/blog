package com.yqh.forum.model;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

// @Data 注解：自动生成 getter/setter/toString/equals/hashCode 方法
// @Entity：实体类
// @Table(name = "users")：数据库表名
// 使用了JPA
@Data
@Entity
@Table(name = "users")
public class User {
//    @Id：主键
    @Id
//    @GeneratedValue：主键生成策略, strategy = GenerationType.IDENTITY: 主键自增
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//   nullable = false: 不能为空，unique = true: 唯一性
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

//    name = "avatar_url"：数据库字段名
    @Column(name = "avatar_url")
    private String avatar = "/images/default-avatar.png";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
} 