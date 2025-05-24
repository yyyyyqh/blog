package com.yqh.forum.service.impl;

import com.yqh.forum.dto.CategoryDTO;
import com.yqh.forum.dto.PostDTO;
import com.yqh.forum.dto.UserDTO;
import com.yqh.forum.exception.ResourceNotFoundException;
import com.yqh.forum.model.Category;
import com.yqh.forum.model.Post;
import com.yqh.forum.model.User;
import com.yqh.forum.repository.CategoryRepository;
import com.yqh.forum.repository.PostRepository;
import com.yqh.forum.repository.UserRepository;
import com.yqh.forum.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheConfig; // 导入 CacheConfig

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional // 保持事务注解
// @CacheConfig(cacheNames = {"posts"}) // 可以在类级别定义默认缓存名，但这里我们更精细地定义了每个方法的缓存名
public class PostServiceImpl implements PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    //对摘要显示的内容进行过滤
    //1.过滤了markdown格式的图片2.过滤html图片标记
    private static final Pattern IMAGE_PATTERN = Pattern.compile(
            "!\\[.*?\\]\\(.*?\\)|<img[^>]*?src=[\"'](.*?)\"'.*?>|#",
            Pattern.CASE_INSENSITIVE
    );

    //过滤Markdown标题的#符号
    private static final Pattern MARKDOWN_HEADING_PATTERN = Pattern.compile(
            "^[#]+", // 匹配行首的一个或多个 # 符号
            Pattern.MULTILINE
    );


    private String generateSummaryContent(String fullContent) {
        if (fullContent == null) {
            return "";
        }
        //1.过滤markdown格式的图片
        Matcher imageMatcher = IMAGE_PATTERN.matcher(fullContent);
        String contentWithoutImages = imageMatcher.replaceAll("");
        //2.过滤Markdown标题的#符号
        Matcher headingMatcher = MARKDOWN_HEADING_PATTERN.matcher(contentWithoutImages);
        String contentWithoutHeadings = headingMatcher.replaceAll("");

        int maxLength = 200;
        if (contentWithoutHeadings.length() > maxLength) {
            return contentWithoutHeadings.substring(0, maxLength) + "...";
        }
        return contentWithoutHeadings;
    }

    @Override
    public PostDTO createPost(PostDTO postDTO, String username) {
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        Category category = categoryRepository.findById(postDTO.getCategory().getId())
                .orElseThrow(() -> new ResourceNotFoundException("分类不存在"));

        Post post = new Post();
        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());
        post.setAuthor(author);
        post.setCategory(category);
        post.setViewCount(0);

        Post savedPost = postRepository.save(post);
        return convertToDTO(savedPost);
    }

    @Override
    public PostDTO updatePost(Long id, PostDTO postDTO, String username) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("帖子不存在"));

        if (!post.getAuthor().getUsername().equals(username)) {
            throw new IllegalStateException("您没有权限编辑此帖子");
        }

        Category category = categoryRepository.findById(postDTO.getCategory().getId())
                .orElseThrow(() -> new ResourceNotFoundException("分类不存在"));

        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());
        post.setCategory(category);

        Post updatedPost = postRepository.save(post);
        return convertToDTO(updatedPost);
    }

    @Override
    public void deletePost(Long id, String username) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("帖子不存在"));

        if (!post.getAuthor().getUsername().equals(username)) {
            throw new IllegalStateException("您没有权限删除此帖子");
        }

        postRepository.delete(post);
    }

    @Override
    public PostDTO findById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("帖子不存在"));
        return convertToDTO(post);
    }

    @Override
    public Page<PostDTO> findAll(Pageable pageable) {
        return postRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Override
    public Page<PostDTO> findByCategoryId(Long categoryId, Pageable pageable) {
        return postRepository.findByCategoryId(categoryId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public Page<PostDTO> search(String keyword, Pageable pageable) {
        return postRepository.findByTitleContainingOrContentContaining(keyword, keyword, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional
    public void incrementViewCount(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("帖子不存在"));
        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);
    }

    @Override
    public Page<PostDTO> findByAuthor(Long userId, Pageable pageable) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        return postRepository.findByAuthor(author, pageable)
                .map(this::convertToDTO);
    }

    private PostDTO convertToDTO(Post post) {
        if (post == null) {
            return null;
        }

        PostDTO dto = new PostDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setSummaryContent(generateSummaryContent(post.getContent()));

        dto.setAuthor(convertToUserDTO(post.getAuthor()));
        dto.setCategory(convertToCategoryDTO(post.getCategory()));
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        dto.setViewCount(post.getViewCount());
        return dto;
    }

    private UserDTO convertToUserDTO(User user) {
        if (user == null) {
            return null;
        }

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        // 如果 UserDTO 有 avatar 字段，这里也需要设置
        // dto.setAvatar(user.getAvatar());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }

    private CategoryDTO convertToCategoryDTO(Category category) {
        if (category == null) {
            return null;
        }

        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        return dto;
    }
}