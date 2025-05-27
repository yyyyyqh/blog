package com.yqh.forum.repository;

import com.yqh.forum.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // 获取在指定日期范围内每天的用户注册数量
    @Query("SELECT DATE(u.createdAt) as registrationDate, COUNT(u.id) as count " +
            "FROM User u " +
            "WHERE u.createdAt >= :startDate AND u.createdAt < :endDate " +
            "GROUP BY DATE(u.createdAt) " + // 在 GROUP BY 中也使用 DATE()
            "ORDER BY registrationDate ASC")
    List<Object[]> countUsersRegisteredByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    Long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
} 