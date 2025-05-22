package com.yqh.forum.service;

import com.yqh.forum.dto.PostDTO;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching; // <-- 导入这个新的注解
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {

    @CacheEvict(value = {"posts", "postsByCategoryId", "postsByAuthor"}, allEntries = true)
    PostDTO createPost(PostDTO postDTO, String username);

    @CacheEvict(value = {"post", "posts", "postsByCategoryId", "postsByAuthor"}, key = "#id")
    PostDTO updatePost(Long id, PostDTO postDTO, String username);

    @CacheEvict(value = {"post", "posts", "postsByCategoryId", "postsByAuthor"}, key = "#id")
    void deletePost(Long id, String username);

    @Cacheable(value = "post", key = "#id")
    PostDTO findById(Long id);

    @Cacheable(value = "posts", key = "{#pageable.pageNumber, #pageable.pageSize, #pageable.sort}")
    Page<PostDTO> findAll(Pageable pageable);

    @Cacheable(value = "postsByCategoryId", key = "{#categoryId, #pageable.pageNumber, #pageable.pageSize, #pageable.sort}")
    Page<PostDTO> findByCategoryId(Long categoryId, Pageable pageable);

    @Cacheable(value = "searchPosts", key = "{#keyword, #pageable.pageNumber, #pageable.pageSize, #pageable.sort}")
    Page<PostDTO> search(String keyword, Pageable pageable);

    // **修改点：将两个 @CacheEvict 合并到 @Caching 中**
    @Caching(evict = {
            @CacheEvict(value = "post", key = "#id"), // 清除单个帖子缓存
            @CacheEvict(value = {"posts", "postsByCategoryId", "postsByAuthor"}, allEntries = true) // 清除所有列表缓存
    })
    void incrementViewCount(Long id);

    @Cacheable(value = "postsByAuthor", key = "{#userId, #pageable.pageNumber, #pageable.pageSize, #pageable.sort}")
    Page<PostDTO> findByAuthor(Long userId, Pageable pageable);
}

//package com.yqh.forum.service;
//
//import com.yqh.forum.dto.PostDTO;
//import com.yqh.forum.model.Post;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//
//public interface PostService {
//    PostDTO createPost(PostDTO postDTO, String username);
//    PostDTO updatePost(Long id, PostDTO postDTO, String username);
//    void deletePost(Long id, String username);
//    PostDTO findById(Long id);
//    Page<PostDTO> findAll(Pageable pageable);
//    Page<PostDTO> findByAuthor(Long userId, Pageable pageable);
//    Page<PostDTO> findByCategoryId(Long categoryId, Pageable pageable);
//    Page<PostDTO> search(String keyword, Pageable pageable);
//    void incrementViewCount(Long id);
//
//}