package com.yqh.forum.service;

import com.yqh.forum.dto.PostDTO;
import com.yqh.forum.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {
    PostDTO createPost(PostDTO postDTO, String username);
    PostDTO updatePost(Long id, PostDTO postDTO, String username);
    void deletePost(Long id, String username);
    PostDTO findById(Long id);
    Page<PostDTO> findAll(Pageable pageable);
    Page<PostDTO> findByAuthor(Long userId, Pageable pageable);
    Page<PostDTO> findByCategoryId(Long categoryId, Pageable pageable);
    Page<PostDTO> search(String keyword, Pageable pageable);
    void incrementViewCount(Long id);

} 