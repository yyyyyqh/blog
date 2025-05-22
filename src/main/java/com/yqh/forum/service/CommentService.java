package com.yqh.forum.service;

import com.yqh.forum.dto.CommentDTO;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {
    // 创建评论后，清除该帖子评论列表的缓存
    @CacheEvict(value = "commentsByPost", key = "#commentDTO.postId", allEntries = true) // allEntries=true 确保清除分页的所有条目
    CommentDTO createComment(CommentDTO commentDTO);
    // 更新评论后，清除该帖子评论列表的缓存
    @CacheEvict(value = "commentsByPost", key = "#commentDTO.postId", allEntries = true)
    CommentDTO updateComment(Long id, CommentDTO commentDTO);
    // 删除评论后，清除该帖子评论列表的缓存
    // 注意：这里的 #id 是评论ID，但我们需要帖子的ID。
    // 可以在方法内部先查出评论的postId，然后手动清除，或者在DTO中包含postId
    // 另一种策略是使用 @CacheEvict(value = "commentsByPost", allEntries = true) 清除所有评论列表缓存（粗粒度）
    @CacheEvict(value = "commentsByPost", allEntries = true) // 粗粒度清除，如果需要精细控制，请看下面的说明
    void deleteComment(Long id);
    // 缓存某个帖子的评论列表
    // value = "commentsByPost" 是缓存的名称
    // key = "#postId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize" 构成了唯一的缓存键，考虑了 postId、页码和每页大小
    // unless = "#result == null || #result.isEmpty()" 表示如果结果为空或null，则不缓存
    @Cacheable(value = "commentsByPost", key = "#postId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize", unless = "#result == null || #result.isEmpty()")
    Page<CommentDTO> findByPostId(Long postId, Pageable pageable);
} 