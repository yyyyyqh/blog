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

    private static final Pattern IMAGE_PATTERN = Pattern.compile(
            "!\\[.*?\\]\\(.*?\\)|<img[^>]*?src=[\"'](.*?)\"'.*?>",
            Pattern.CASE_INSENSITIVE
    );

    private String generateSummaryContent(String fullContent) {
        if (fullContent == null) {
            return "";
        }
        Matcher matcher = IMAGE_PATTERN.matcher(fullContent);
        String contentWithoutImages = matcher.replaceAll("");

        int maxLength = 200;
        if (contentWithoutImages.length() > maxLength) {
            return contentWithoutImages.substring(0, maxLength) + "...";
        }
        return contentWithoutImages;
    }

    @Override
    // @CacheEvict(value = {"posts", "postsByCategoryId", "postsByAuthor"}, allEntries = true) // 接口已定义，此处无需重复
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
    // @CacheEvict(value = {"post", "posts", "postsByCategoryId", "postsByAuthor"}, key = "#id") // 接口已定义，此处无需重复
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
    // @CacheEvict(value = {"post", "posts", "postsByCategoryId", "postsByAuthor"}, key = "#id") // 接口已定义，此处无需重复
    public void deletePost(Long id, String username) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("帖子不存在"));

        if (!post.getAuthor().getUsername().equals(username)) {
            throw new IllegalStateException("您没有权限删除此帖子");
        }

        postRepository.delete(post);
    }

    @Override
    // @Cacheable(value = "post", key = "#id") // 接口已定义，此处无需重复
    public PostDTO findById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("帖子不存在"));
        return convertToDTO(post);
    }

    @Override
    // @Cacheable(value = "posts", key = "{#pageable.pageNumber, #pageable.pageSize, #pageable.sort}") // 接口已定义，此处无需重复
    public Page<PostDTO> findAll(Pageable pageable) {
        return postRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Override
    // @Cacheable(value = "postsByCategoryId", key = "{#categoryId, #pageable.pageNumber, #pageable.pageSize, #pageable.sort}") // 接口已定义，此处无需重复
    public Page<PostDTO> findByCategoryId(Long categoryId, Pageable pageable) {
        return postRepository.findByCategoryId(categoryId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    // @Cacheable(value = "searchPosts", key = "{#keyword, #pageable.pageNumber, #pageable.pageSize, #pageable.sort}") // 接口已定义，此处无需重复
    public Page<PostDTO> search(String keyword, Pageable pageable) {
        return postRepository.findByTitleContainingOrContentContaining(keyword, keyword, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional
    // @CacheEvict(value = "post", key = "#id") // 接口已定义，此处无需重复
    // @CacheEvict(value = {"posts", "postsByCategoryId", "postsByAuthor"}, allEntries = true) // 接口已定义，此处无需重复
    public void incrementViewCount(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("帖子不存在"));
        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);
    }

    @Override
    // @Cacheable(value = "postsByAuthor", key = "{#userId, #pageable.pageNumber, #pageable.pageSize, #pageable.sort}") // 接口已定义，此处无需重复
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

//package com.yqh.forum.service.impl;
//
//import com.yqh.forum.dto.CategoryDTO;
//import com.yqh.forum.dto.PostDTO;
//import com.yqh.forum.dto.UserDTO;
//import com.yqh.forum.exception.ResourceNotFoundException;
//import com.yqh.forum.model.Category;
//import com.yqh.forum.model.Post;
//import com.yqh.forum.model.User;
//import com.yqh.forum.repository.CategoryRepository;
//import com.yqh.forum.repository.PostRepository;
//import com.yqh.forum.repository.UserRepository;
//import com.yqh.forum.service.PostService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//@Service
//public class PostServiceImpl implements PostService {
//
//    @Autowired
//    private PostRepository postRepository;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private CategoryRepository categoryRepository;
//
//    // **修改：用于匹配 Markdown 图片链接 或 HTML 图片标签 的正则表达式**
//    // Markdown 链接: !\[.*?\]\(.*?\)
//    // HTML 标签: <img.*?src=[\"'].*?[\"'].*?>
//    // 使用 | 连接两个模式，实现“或”的功能。Pattern.CASE_INSENSITIVE 忽略大小写
//    private static final Pattern IMAGE_PATTERN = Pattern.compile("!\\[.*?\\]\\(.*?\\)|<img.*?src=[\"'].*?[\"'].*?>", Pattern.CASE_INSENSITIVE);
//
//    /**
//     * **修改：根据完整的帖子内容生成用于列表显示的摘要内容**
//     * 移除 Markdown 图片链接 和 HTML 图片标签，并可以截取长度
//     * @param fullContent 完整的原始 Markdown 内容
//     * @return 处理后的摘要内容
//     */
//    private String generateSummaryContent(String fullContent) {
//        if (fullContent == null) {
//            return "";
//        }
//        // **使用新的正则表达式移除图片相关的标记**
//        Matcher matcher = IMAGE_PATTERN.matcher(fullContent);
//        String contentWithoutImages = matcher.replaceAll("");
//
//        // **可选：截取摘要内容的长度**
//        int maxLength = 200; // 与 list.html 中原有的 #strings.abbreviate(post.content, 200) 匹配
//        if (contentWithoutImages.length() > maxLength) {
//            // 截取并添加省略号
//            return contentWithoutImages.substring(0, maxLength) + "...";
//        }
//
//        return contentWithoutImages; // 返回处理后的内容
//    }
//
//    // ... createPost, updatePost, deletePost, findById, findAll, findByCategoryId, search, incrementViewCount, findByAuthor, convertToDTO, convertToUserDTO, convertToCategoryDTO 方法保持不变 ...
//    // 请确保这些方法在您的文件中都存在且正确
//
//    // 您可以复制粘贴 generateSummaryContent 方法上方到类结尾的所有方法，
//    // 并用上面提供的完整 PostServiceImpl 代码替换您文件中的对应方法。
//    // 确保引入的包是正确的。
//
//    @Override
//    @Transactional
//    public PostDTO createPost(PostDTO postDTO, String username) {
//        User author = userRepository.findByUsername(username)
//                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
//
//        Category category = categoryRepository.findById(postDTO.getCategory().getId())
//                .orElseThrow(() -> new ResourceNotFoundException("分类不存在"));
//
//        Post post = new Post();
//        post.setTitle(postDTO.getTitle());
//        post.setContent(postDTO.getContent());
//        post.setAuthor(author);
//        post.setCategory(category);
//        // Set initial view count
//        post.setViewCount(0);
//
//        Post savedPost = postRepository.save(post);
//        return convertToDTO(savedPost);
//    }
//
//    @Override
//    @Transactional
//    public PostDTO updatePost(Long id, PostDTO postDTO, String username) {
//        Post post = postRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("帖子不存在"));
//
//        if (!post.getAuthor().getUsername().equals(username)) {
//            throw new IllegalStateException("您没有权限编辑此帖子");
//        }
//
//        Category category = categoryRepository.findById(postDTO.getCategory().getId())
//                .orElseThrow(() -> new ResourceNotFoundException("分类不存在"));
//
//        post.setTitle(postDTO.getTitle());
//        post.setContent(postDTO.getContent());
//        post.setCategory(category);
//
//        Post updatedPost = postRepository.save(post);
//        return convertToDTO(updatedPost);
//    }
//
//    @Override
//    @Transactional
//    public void deletePost(Long id, String username) {
//        Post post = postRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("帖子不存在"));
//
//        if (!post.getAuthor().getUsername().equals(username)) {
//            throw new IllegalStateException("您没有权限删除此帖子");
//        }
//
//        postRepository.delete(post);
//    }
//
//    @Override
//    public PostDTO findById(Long id) {
//        Post post = postRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("帖子不存在"));
//        // 返回包含完整 content 的 DTO
//        return convertToDTO(post);
//    }
//
//    @Override
//    public Page<PostDTO> findAll(Pageable pageable) {
//        return postRepository.findAll(pageable)
//                // 使用 map 调用 convertToDTO，其中会生成并设置 summaryContent
//                .map(this::convertToDTO);
//    }
//
//    @Override
//    public Page<PostDTO> findByCategoryId(Long categoryId, Pageable pageable) {
//        return postRepository.findByCategoryId(categoryId, pageable)
//                // 使用 map 调用 convertToDTO，其中会生成并设置 summaryContent
//                .map(this::convertToDTO);
//    }
//
//    @Override
//    public Page<PostDTO> search(String keyword, Pageable pageable) {
//        return postRepository.findByTitleContainingOrContentContaining(keyword, keyword, pageable)
//                .map(this::convertToDTO); // map 调用 convertToDTO，其中会生成并设置 summaryContent
//    }
//
//    @Override
//    @Transactional
//    public void incrementViewCount(Long id) {
//        Post post = postRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("帖子不存在"));
//        post.setViewCount(post.getViewCount() + 1);
//        postRepository.save(post);
//    }
//
//    @Override
//    public Page<PostDTO> findByAuthor(Long userId, Pageable pageable) {
//        // 根据用户 ID 查找用户实体
//        User author = userRepository.findById(userId)
//                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
//        // 调用 Repository 方法按作者查找帖子，并使用 convertToDTO 转换 (包含摘要生成)
//        return postRepository.findByAuthor(author, pageable)
//                .map(this::convertToDTO); // map 调用 convertToDTO，其中会生成并设置 summaryContent
//    }
//
//    // **修改 convertToDTO 方法，添加生成和设置 summaryContent 的逻辑**
//    private PostDTO convertToDTO(Post post) {
//        if (post == null) {
//            return null;
//        }
//
//        PostDTO dto = new PostDTO();
//        dto.setId(post.getId());
//        dto.setTitle(post.getTitle());
//        dto.setContent(post.getContent()); // 保留原始完整内容
//        // **调用 generateSummaryContent 生成摘要并设置到 DTO 中**
//        dto.setSummaryContent(generateSummaryContent(post.getContent()));
//
//        dto.setAuthor(convertToUserDTO(post.getAuthor()));
//        dto.setCategory(convertToCategoryDTO(post.getCategory()));
//        dto.setCreatedAt(post.getCreatedAt());
//        dto.setUpdatedAt(post.getUpdatedAt());
//        dto.setViewCount(post.getViewCount());
//        return dto;
//    }
//
//    private UserDTO convertToUserDTO(User user) {
//        if (user == null) {
//            return null;
//        }
//
//        UserDTO dto = new UserDTO();
//        dto.setId(user.getId());
//        dto.setUsername(user.getUsername());
//        dto.setEmail(user.getEmail());
//        dto.setCreatedAt(user.getCreatedAt());
//        dto.setUpdatedAt(user.getUpdatedAt());
//        return dto;
//    }
//
//    private CategoryDTO convertToCategoryDTO(Category category) {
//        if (category == null) {
//            return null;
//        }
//
//        CategoryDTO dto = new CategoryDTO();
//        dto.setId(category.getId());
//        dto.setName(category.getName());
//        dto.setDescription(category.getDescription());
//        dto.setCreatedAt(category.getCreatedAt());
//        dto.setUpdatedAt(category.getUpdatedAt());
//        return dto;
//    }
//}