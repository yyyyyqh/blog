package com.yqh.forum.repository;

import com.yqh.forum.model.Comment;
import com.yqh.forum.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByPost(Post post, Pageable pageable);
    Page<Comment> findByPostOrderByCreatedAtDesc(Post post, Pageable pageable);
} 