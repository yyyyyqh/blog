package com.yqh.forum.service.impl; // 与 PostServiceImpl 相同的包

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // 启用 Mockito 扩展
@DisplayName("PostServiceImpl 单元测试") // 为测试类添加一个显示名称
class PostServiceImplTest {

    @Mock // 创建 PostRepository 的 Mock 对象
    private PostRepository postRepository;

    @Mock // 创建 UserRepository 的 Mock 对象
    private UserRepository userRepository;

    @Mock // 创建 CategoryRepository 的 Mock 对象
    private CategoryRepository categoryRepository;

    @InjectMocks // 创建 PostServiceImpl 的实例，并将上面 @Mock 注解的依赖注入进去
    private PostServiceImpl postService;

    private User testUser;
    private Category testCategory;
    private Post testPost;
    private PostDTO testPostDTO;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        // 初始化通用的测试数据
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password"); // 实际项目中密码应该加密
        testUser.setCreatedAt(LocalDateTime.now().minusDays(1));
        testUser.setUpdatedAt(LocalDateTime.now().minusDays(1));

        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("技术分享");
        testCategory.setDescription("关于技术的分享");
        testCategory.setCreatedAt(LocalDateTime.now().minusDays(1));
        testCategory.setUpdatedAt(LocalDateTime.now().minusDays(1));

        testPost = new Post();
        testPost.setId(1L);
        testPost.setTitle("我的第一篇帖子");
        testPost.setContent("这是帖子的内容，包含一些 #Markdown标题 和 ![图片](http://example.com/image.png) 以及足够长的文本用于测试摘要生成，需要超过两百个字符才能触发截断效果，所以我们在这里添加更多更多的文本。");
        testPost.setAuthor(testUser);
        testPost.setCategory(testCategory);
        testPost.setViewCount(10);
        testPost.setCreatedAt(LocalDateTime.now());
        testPost.setUpdatedAt(LocalDateTime.now());

        testPostDTO = new PostDTO();
        testPostDTO.setTitle("DTO 标题");
        testPostDTO.setContent("DTO 内容");
        UserDTO authorDTO = new UserDTO();
        authorDTO.setId(testUser.getId());
        authorDTO.setUsername(testUser.getUsername());
        testPostDTO.setAuthor(authorDTO);
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setId(testCategory.getId());
        categoryDTO.setName(testCategory.getName());
        testPostDTO.setCategory(categoryDTO);

        pageable = PageRequest.of(0, 10); // 默认分页参数
    }

    @Nested
    @DisplayName("创建帖子 (createPost)")
    class CreatePostTests {
        @Test
        @DisplayName("当用户和分类存在时，成功创建帖子")
        void createPost_whenUserAndCategoryExist_shouldSucceed() {
            // 准备 Mock 行为
            when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
            when(categoryRepository.findById(testPostDTO.getCategory().getId())).thenReturn(Optional.of(testCategory));
            // 模拟 postRepository.save() 方法的行为
            when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
                Post postToSave = invocation.getArgument(0);
                postToSave.setId(2L); // 模拟数据库生成ID
                postToSave.setCreatedAt(LocalDateTime.now()); // 模拟设置创建时间
                postToSave.setUpdatedAt(LocalDateTime.now()); // 模拟设置更新时间
                return postToSave;
            });

            // 执行被测试方法
            PostDTO createdPost = postService.createPost(testPostDTO, testUser.getUsername());

            // 断言结果
            assertNotNull(createdPost);
            assertEquals(testPostDTO.getTitle(), createdPost.getTitle());
            assertEquals(testUser.getUsername(), createdPost.getAuthor().getUsername());
            assertEquals(testCategory.getName(), createdPost.getCategory().getName());
            assertEquals(0, createdPost.getViewCount()); // 初始浏览量为0
            assertNotNull(createdPost.getSummaryContent());
            assertFalse(createdPost.getSummaryContent().contains("![图片]")); // 验证摘要过滤
            assertFalse(createdPost.getSummaryContent().contains("#"));     // 验证摘要过滤

            // 验证 Mock 对象的方法调用次数
            verify(userRepository).findByUsername(testUser.getUsername());
            verify(categoryRepository).findById(testPostDTO.getCategory().getId());
            verify(postRepository).save(any(Post.class));
        }

        @Test
        @DisplayName("当用户不存在时，抛出 ResourceNotFoundException")
        void createPost_whenUserNotFound_shouldThrowResourceNotFoundException() {
            when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> postService.createPost(testPostDTO, "unknownUser"));

            assertEquals("用户不存在", exception.getMessage());
            verify(userRepository).findByUsername("unknownUser");
            verify(categoryRepository, never()).findById(anyLong()); // 分类查询不应被调用
            verify(postRepository, never()).save(any(Post.class));   // 保存不应被调用
        }

        @Test
        @DisplayName("当分类不存在时，抛出 ResourceNotFoundException")
        void createPost_whenCategoryNotFound_shouldThrowResourceNotFoundException() {
            when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
            when(categoryRepository.findById(testPostDTO.getCategory().getId())).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> postService.createPost(testPostDTO, testUser.getUsername()));

            assertEquals("分类不存在", exception.getMessage());
            verify(userRepository).findByUsername(testUser.getUsername());
            verify(categoryRepository).findById(testPostDTO.getCategory().getId());
            verify(postRepository, never()).save(any(Post.class));
        }
    }

    @Nested
    @DisplayName("更新帖子 (updatePost)")
    class UpdatePostTests {
        @Test
        @DisplayName("当帖子存在且用户为作者时，成功更新帖子")
        void updatePost_whenAuthorized_shouldSucceed() {
            when(postRepository.findById(testPost.getId())).thenReturn(Optional.of(testPost)); // 确保返回的 testPost 包含正确的 author
            when(categoryRepository.findById(testPostDTO.getCategory().getId())).thenReturn(Optional.of(testCategory));
            when(postRepository.save(any(Post.class))).thenReturn(testPost); // save 后返回更新的 post

            PostDTO updatedPostDTO = postService.updatePost(testPost.getId(), testPostDTO, testUser.getUsername());

            assertNotNull(updatedPostDTO);
            assertEquals(testPostDTO.getTitle(), updatedPostDTO.getTitle()); // 验证标题已更新
            assertEquals(testPostDTO.getContent(), updatedPostDTO.getContent()); // 验证内容已更新
            assertEquals(testCategory.getName(), updatedPostDTO.getCategory().getName()); // 验证分类已更新

            verify(postRepository).findById(testPost.getId());
            verify(categoryRepository).findById(testPostDTO.getCategory().getId());
            verify(postRepository).save(testPost);
        }

        @Test
        @DisplayName("当帖子不存在时，抛出 ResourceNotFoundException")
        void updatePost_whenPostNotFound_shouldThrowResourceNotFoundException() {
            when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> postService.updatePost(99L, testPostDTO, testUser.getUsername()));

            assertEquals("帖子不存在", exception.getMessage());
            verify(postRepository).findById(99L);
        }

        @Test
        @DisplayName("当用户不是作者时，抛出 IllegalStateException")
        void updatePost_whenNotAuthor_shouldThrowIllegalStateException() {
            User anotherUser = new User();
            anotherUser.setUsername("anotherUser");
            testPost.setAuthor(anotherUser); // 设置帖子的作者为其他人

            when(postRepository.findById(testPost.getId())).thenReturn(Optional.of(testPost));

            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> postService.updatePost(testPost.getId(), testPostDTO, testUser.getUsername())); // 当前用户是 testUser

            assertEquals("您没有权限编辑此帖子", exception.getMessage());
            verify(postRepository).findById(testPost.getId());
        }
    }


    @Nested
    @DisplayName("删除帖子 (deletePost)")
    class DeletePostTests {
        @Test
        @DisplayName("当帖子存在且用户为作者时，成功删除帖子")
        void deletePost_whenAuthorized_shouldSucceed() {
            when(postRepository.findById(testPost.getId())).thenReturn(Optional.of(testPost));
            doNothing().when(postRepository).delete(testPost); // 模拟删除操作

            assertDoesNotThrow(() -> postService.deletePost(testPost.getId(), testUser.getUsername()));

            verify(postRepository).findById(testPost.getId());
            verify(postRepository).delete(testPost);
        }

        @Test
        @DisplayName("当帖子不存在时，抛出 ResourceNotFoundException")
        void deletePost_whenPostNotFound_shouldThrowResourceNotFoundException() {
            when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> postService.deletePost(99L, testUser.getUsername()));

            assertEquals("帖子不存在", exception.getMessage());
            verify(postRepository).findById(99L);
        }

        @Test
        @DisplayName("当用户不是作者时，抛出 IllegalStateException")
        void deletePost_whenNotAuthor_shouldThrowIllegalStateException() {
            User anotherUser = new User();
            anotherUser.setUsername("anotherUser");
            testPost.setAuthor(anotherUser);

            when(postRepository.findById(testPost.getId())).thenReturn(Optional.of(testPost));

            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> postService.deletePost(testPost.getId(), testUser.getUsername()));

            assertEquals("您没有权限删除此帖子", exception.getMessage());
            verify(postRepository).findById(testPost.getId());
        }
    }


    @Nested
    @DisplayName("查找帖子 (findById, findAll, etc.)")
    class FindPostTests {
        @Test
        @DisplayName("通过ID查找存在的帖子，返回PostDTO")
        void findById_whenPostExists_shouldReturnPostDTO() {
            when(postRepository.findById(testPost.getId())).thenReturn(Optional.of(testPost));

            PostDTO foundDTO = postService.findById(testPost.getId());

            assertNotNull(foundDTO);
            assertEquals(testPost.getTitle(), foundDTO.getTitle());
            assertEquals(testUser.getUsername(), foundDTO.getAuthor().getUsername());
            verify(postRepository).findById(testPost.getId());
        }

        @Test
        @DisplayName("通过ID查找不存在的帖子，抛出ResourceNotFoundException")
        void findById_whenPostNotExists_shouldThrowResourceNotFoundException() {
            when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

            //验证在执行某段代码时是否会按预期抛出特定类型的异常
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> postService.findById(99L));
            //验证时候符合预期的错误信息
            assertEquals("帖子不存在", exception.getMessage());
        }

        @Test
        @DisplayName("查找所有帖子，返回PostDTO分页")
        void findAll_shouldReturnPageOfPostDTO() {
            Page<Post> postPage = new PageImpl<>(Collections.singletonList(testPost), pageable, 1);
            when(postRepository.findAll(pageable)).thenReturn(postPage);

            Page<PostDTO> resultDTOPage = postService.findAll(pageable);

            assertNotNull(resultDTOPage);
            assertEquals(1, resultDTOPage.getTotalElements());
            assertEquals(testPost.getTitle(), resultDTOPage.getContent().get(0).getTitle());
            verify(postRepository).findAll(pageable);
        }

        @Test
        @DisplayName("根据分类ID查找帖子，返回PostDTO分页")
        void findByCategoryId_shouldReturnPageOfPostDTO() {
            Page<Post> postPage = new PageImpl<>(Collections.singletonList(testPost), pageable, 1);
            when(postRepository.findByCategoryId(testCategory.getId(), pageable)).thenReturn(postPage);

            Page<PostDTO> resultDTOPage = postService.findByCategoryId(testCategory.getId(), pageable);

            assertNotNull(resultDTOPage);
            assertEquals(1, resultDTOPage.getTotalElements());
            verify(postRepository).findByCategoryId(testCategory.getId(), pageable);
        }

        @Test
        @DisplayName("根据作者ID查找帖子，返回PostDTO分页")
        void findByAuthor_whenAuthorExists_shouldReturnPageOfPostDTO() {
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            Page<Post> postPage = new PageImpl<>(Collections.singletonList(testPost), pageable, 1);
            when(postRepository.findByAuthor(testUser, pageable)).thenReturn(postPage);

            Page<PostDTO> resultDTOPage = postService.findByAuthor(testUser.getId(), pageable);

            assertNotNull(resultDTOPage);
            assertEquals(1, resultDTOPage.getTotalElements());
            verify(userRepository).findById(testUser.getId());
            verify(postRepository).findByAuthor(testUser, pageable);
        }

        @Test
        @DisplayName("根据作者ID查找帖子，当作者不存在时抛出异常")
        void findByAuthor_whenAuthorNotExists_shouldThrowResourceNotFoundException() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> postService.findByAuthor(99L, pageable));
            assertEquals("用户不存在", exception.getMessage());
            verify(userRepository).findById(99L);
            verify(postRepository, never()).findByAuthor(any(User.class), any(Pageable.class));
        }


        @Test
        @DisplayName("搜索帖子，返回匹配的PostDTO分页")
        void search_shouldReturnMatchingPageOfPostDTO() {
            String keyword = "第一篇";
            Page<Post> postPage = new PageImpl<>(Collections.singletonList(testPost), pageable, 1);
            when(postRepository.findByTitleContainingOrContentContaining(keyword, keyword, pageable)).thenReturn(postPage);

            Page<PostDTO> resultDTOPage = postService.search(keyword, pageable);

            assertNotNull(resultDTOPage);
            assertEquals(1, resultDTOPage.getTotalElements());
            assertTrue(resultDTOPage.getContent().get(0).getTitle().contains(keyword));
            verify(postRepository).findByTitleContainingOrContentContaining(keyword, keyword, pageable);
        }
    }


    @Nested
    @DisplayName("增加帖子浏览次数 (incrementViewCount)")
    class IncrementViewCountTests {
        @Test
        @DisplayName("当帖子存在时，成功增加浏览次数")
        void incrementViewCount_whenPostExists_shouldIncrement() {
            when(postRepository.findById(testPost.getId())).thenReturn(Optional.of(testPost));
            // 模拟 save 方法，因为它是 void，我们不需要 thenReturn，但可以验证它被调用
            // 或者如果 save 返回更新后的实体，可以像下面这样：
            when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));


            int initialCount = testPost.getViewCount();
            postService.incrementViewCount(testPost.getId());

            assertEquals(initialCount + 1, testPost.getViewCount()); // 验证原对象被修改
            verify(postRepository).findById(testPost.getId());
            verify(postRepository).save(testPost); // 验证修改后的对象被保存
        }

        @Test
        @DisplayName("当帖子不存在时，抛出ResourceNotFoundException")
        void incrementViewCount_whenPostNotExists_shouldThrowResourceNotFoundException() {
            when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> postService.incrementViewCount(99L));
            assertEquals("帖子不存在", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("摘要生成 (generateSummaryContent - 间接测试)")
    class GenerateSummaryContentTests {
        @Test
        @DisplayName("内容为null时，摘要为空字符串")
        void generateSummary_whenContentIsNull_shouldReturnEmptyString() {
            testPostDTO.setContent(null); // 设置测试内容
            when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
            when(categoryRepository.findById(testPostDTO.getCategory().getId())).thenReturn(Optional.of(testCategory));
            when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

            PostDTO result = postService.createPost(testPostDTO, testUser.getUsername());
            assertEquals("", result.getSummaryContent());
        }

        @Test
        @DisplayName("短内容，摘要为原始内容")
        void generateSummary_whenContentIsShort_shouldReturnOriginal() {
            String shortContent = "简短内容。";
            testPostDTO.setContent(shortContent);
            when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
            when(categoryRepository.findById(testPostDTO.getCategory().getId())).thenReturn(Optional.of(testCategory));
            when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

            PostDTO result = postService.createPost(testPostDTO, testUser.getUsername());
            assertEquals(shortContent, result.getSummaryContent());
        }

        //过滤测试
        @Test
        @DisplayName("长内容，摘要被截断并过滤Markdown")
        void generateSummary_whenContentIsLongWithMarkdown_shouldBeFilteredAndTruncated() {
            String longContent = "这是 ![图片](url) 一段 #非常非常# 长且包含Markdown的内容，它应该被截断并且Markdown标记应该被移除，例如图片链接和标题标记。".repeat(10);
            testPostDTO.setContent(longContent);
            when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
            when(categoryRepository.findById(testPostDTO.getCategory().getId())).thenReturn(Optional.of(testCategory));
            when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

            PostDTO result = postService.createPost(testPostDTO, testUser.getUsername());

            assertFalse(result.getSummaryContent().contains("![图片]"));
            assertFalse(result.getSummaryContent().contains("#"));
            assertTrue(result.getSummaryContent().endsWith("..."));
            assertTrue(result.getSummaryContent().length() <= 203); // 200 + "..."
        }
    }
}