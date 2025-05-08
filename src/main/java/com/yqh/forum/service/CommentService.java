package com.yqh.forum.service;

import com.yqh.forum.dto.CommentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {
    CommentDTO createComment(CommentDTO commentDTO);
    CommentDTO updateComment(Long id, CommentDTO commentDTO);
    void deleteComment(Long id);
    Page<CommentDTO> findByPostId(Long postId, Pageable pageable);
} 