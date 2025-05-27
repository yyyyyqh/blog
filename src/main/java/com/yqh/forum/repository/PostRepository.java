package com.yqh.forum.repository;

import com.yqh.forum.model.Post;
import com.yqh.forum.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByAuthor(User author, Pageable pageable);
    
    @Query("SELECT p FROM Post p ORDER BY p.createdAt DESC")
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE p.title LIKE %:keyword% OR p.content LIKE %:keyword%")
    Page<Post> search(String keyword, Pageable pageable);

    Page<Post> findByCategoryId(Long categoryId, Pageable pageable);
    Page<Post> findByTitleContainingOrContentContaining(String title, String content, Pageable pageable);


    // 使用 MySQL 的 DATE() 函数
    @Query("SELECT DATE(p.createdAt) as creationDate, COUNT(p.id) as count " +
            "FROM Post p " +
            "WHERE p.createdAt >= :startDate AND p.createdAt < :endDate " +
            "GROUP BY DATE(p.createdAt) " + // 在 GROUP BY 中也使用 DATE()
            "ORDER BY creationDate ASC")
    List<Object[]> countPostsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    Long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT DISTINCT p.author.id FROM Post p WHERE p.createdAt >= :startDate AND p.createdAt < :endDate")
    List<Long> findDistinctAuthorIdsByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

} 