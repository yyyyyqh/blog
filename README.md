# 论坛系统

## 数据库表结构

### 用户表 (users)
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 分类表 (categories)
```sql
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 帖子表 (posts)
```sql
CREATE TABLE posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    author_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    view_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (author_id) REFERENCES users(id),
    FOREIGN KEY (category_id) REFERENCES categories(id)
);
```

### 评论表 (comments)
```sql
CREATE TABLE comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL,
    post_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(id),
    FOREIGN KEY (author_id) REFERENCES users(id)
);
```

## 默认数据

### 默认分类
- 综合讨论：一般性话题讨论
- 技术交流：技术相关话题讨论
- 生活分享：生活相关话题讨论
- 新闻资讯：新闻相关话题讨论

### 默认管理员账号
- 用户名：admin
- 密码：admin

## 技术栈
- 后端：Spring Boot 2.7.6
- 数据库：MySQL 5.7.42
- 前端：Thymeleaf + Bootstrap 5
- 安全：Spring Security 

# 数据

可以由https://mockaroo.com/生成，或手动输入





```
-- 1. 创建角色表 (roles)
-- 这是存储角色定义本身的表
CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,  -- 角色的唯一 ID，自增长
    name VARCHAR(50) NOT NULL UNIQUE      -- 角色的名称，例如 'ROLE_USER', 'ROLE_ADMIN'，必须唯一且非空
);

-- 2. 创建用户和角色关联表 (user_roles)
-- 这是连接 users 表和 roles 表的中间表
CREATE TABLE user_roles (
    user_id BIGINT,  -- 引用 users 表的主键 ID，类型应与 users.id 一致
    role_id BIGINT,  -- 引用 roles 表的主键 ID，类型应与 roles.id 一致
    PRIMARY KEY (user_id, role_id),  -- 联合主键，确保一个用户-角色组合只出现一次

    -- 外键约束：确保 user_id 必须是 users 表中存在的 id
    -- ON DELETE CASCADE 表示如果 users 表中的用户被删除，user_roles 表中相关的记录也会被自动删除
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    -- 外键约束：确保 role_id 必须是 roles 表中存在的 id
    -- ON DELETE CASCADE 表示如果 roles 表中的角色被删除，user_roles 表中相关的记录也会被自动删除
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- 可选：为外键字段添加索引，通常可以提高查询性能
CREATE INDEX idx_user_roles_user_id ON user_roles (user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles (role_id);

-- 可选：插入一些初始角色数据
-- INSERT INTO roles (name) VALUES ('ROLE_USER');
-- INSERT INTO roles (name) VALUES ('ROLE_ADMIN');
```



```
INSERT INTO roles (name) VALUES ('ROLE_USER'); -- 假设这是普通用户角色
INSERT INTO roles (name) VALUES ('ROLE_ADMIN'); -- 假设这是管理员角色

-- 将用户 A (ID=1) 设置为管理员 (ROLE_ADMIN, ID=1)
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);
```

