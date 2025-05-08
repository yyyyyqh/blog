package com.yqh.forum.service.impl;

import com.yqh.forum.dto.CommentDTO;
import com.yqh.forum.model.Comment;
import com.yqh.forum.model.Post;
import com.yqh.forum.model.User;
import com.yqh.forum.repository.CommentRepository;
import com.yqh.forum.repository.PostRepository;
import com.yqh.forum.service.CommentService;
import com.yqh.forum.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserService userService;

    @Override
    public CommentDTO createComment(CommentDTO commentDTO) {
        User currentUser = userService.getCurrentUser();
        Post post = postRepository.findById(commentDTO.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("帖子不存在"));

        Comment comment = new Comment();
        comment.setContent(commentDTO.getContent());
        comment.setAuthor(currentUser);
        comment.setPost(post);

        Comment savedComment = commentRepository.save(comment);
        return convertToDTO(savedComment);
    }

    @Override
    public CommentDTO updateComment(Long id, CommentDTO commentDTO) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("评论不存在"));

        User currentUser = userService.getCurrentUser();
        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("您没有权限修改此评论");
        }

        comment.setContent(commentDTO.getContent());
        Comment updatedComment = commentRepository.save(comment);
        return convertToDTO(updatedComment);
    }

    @Override
    public void deleteComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("评论不存在"));

        User currentUser = userService.getCurrentUser();
        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("您没有权限删除此评论");
        }

        commentRepository.delete(comment);
    }

    @Override
    public Page<CommentDTO> findByPostId(Long postId, Pageable pageable) {
        Post post = new Post();
        post.setId(postId);
        return commentRepository.findByPostOrderByCreatedAtDesc(post, pageable)
                .map(this::convertToDTO);
    }

    private CommentDTO convertToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setAuthor(userService.findByUsername(comment.getAuthor().getUsername()));
        dto.setPostId(comment.getPost().getId());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        return dto;
    }
} 