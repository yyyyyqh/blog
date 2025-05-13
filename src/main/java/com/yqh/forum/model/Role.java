package com.yqh.forum.model;

import lombok.Data; // 导入 Lombok Data 注解

import javax.persistence.*;


@Data // 使用 @Data 自动生成 getter/setter/equals/hashCode/toString
@Entity // 声明这是 JPA 实体
@Table(name = "roles") // 映射到数据库的 roles 表
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 角色 ID，主键

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name; // 角色名称，例如 "ROLE_USER", "ROLE_ADMIN"


}