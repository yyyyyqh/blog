package com.yqh.forum.service.impl;

import com.yqh.forum.dto.PostDTO;
import com.yqh.forum.model.Post;
import com.yqh.forum.model.User;
import com.yqh.forum.repository.PostRepository;
import com.yqh.forum.service.PostService;
import com.yqh.forum.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PostServiceImpl implements PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserService userService;

    @Override
    public PostDTO createPost(PostDTO postDTO) {
        User currentUser = userService.getCurrentUser();
        Post post = new Post();
        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());
        post.setAuthor(currentUser);
        post.setViewCount(0);

        Post savedPost = postRepository.save(post);
        return convertToDTO(savedPost);
    }

    @Override
    public PostDTO updatePost(Long id, PostDTO postDTO) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("帖子不存在"));

        User currentUser = userService.getCurrentUser();
        if (!post.getAuthor().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("您没有权限修改此帖子");
        }

        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());

        Post updatedPost = postRepository.save(post);
        return convertToDTO(updatedPost);
    }

    @Override
    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("帖子不存在"));

        User currentUser = userService.getCurrentUser();
        if (!post.getAuthor().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("您没有权限删除此帖子");
        }

        postRepository.delete(post);
    }

    @Override
    public PostDTO findById(Long id) {
        return postRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Override
    public Page<PostDTO> findAll(Pageable pageable) {
        return postRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::convertToDTO);
    }

    @Override
    public Page<PostDTO> findByAuthor(Long userId, Pageable pageable) {
        User user = new User();
        user.setId(userId);
        return postRepository.findByAuthor(user, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public Page<PostDTO> search(String keyword, Pageable pageable) {
        return postRepository.search(keyword, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public void incrementViewCount(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("帖子不存在"));
        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);
    }

    private PostDTO convertToDTO(Post post) {
        PostDTO dto = new PostDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setAuthor(userService.findByUsername(post.getAuthor().getUsername()));
        dto.setViewCount(post.getViewCount());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        return dto;
    }
} 