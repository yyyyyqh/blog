package com.yqh.forum.repository;

import com.yqh.forum.model.Comment;
import com.yqh.forum.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByPost(Post post, Pageable pageable);
    Page<Comment> findByPostOrderByCreatedAtDesc(Post post, Pageable pageable);

    // 获取在指定日期范围内每天的评论创建数量
    @Query("SELECT DATE(c.createdAt) as creationDate, COUNT(c.id) as count " +
            "FROM Comment c " +
            "WHERE c.createdAt >= :startDate AND c.createdAt < :endDate " +
            "GROUP BY DATE(c.createdAt) " + // 在 GROUP BY 中也使用 DATE()
            "ORDER BY creationDate ASC")
    List<Object[]> countCommentsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    Long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT DISTINCT c.author.id FROM Comment c WHERE c.createdAt >= :startDate AND c.createdAt < :endDate")
    List<Long> findDistinctAuthorIdsByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
} 