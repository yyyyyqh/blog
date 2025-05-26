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
import org.springframework.data.domain.Sort;


import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq; // 引入 eq
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostServiceImpl 分页功能单元测试")
class PostServiceImplPaginationTest { // 可以创建一个新的测试类，或者在原有测试类中用 @Nested 组织

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    // CategoryRepository 可能在某些分页方法中不需要，但为了完整性先保留
    @Mock
    private CategoryRepository categoryRepository;


    @InjectMocks
    private PostServiceImpl postService;

    private User testUser;
    private Category testCategory;
    private Post testPost1, testPost2;
    private List<Post> postList;
    //private List<Post> allPosts; // 存储所有11篇帖子

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setCreatedAt(LocalDateTime.now());

        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("技术");
        testCategory.setCreatedAt(LocalDateTime.now());

        // 初始化11篇帖子
        postList = new ArrayList<>();
        for (int i = 1; i <= 11; i++) {
            Post post = new Post();
            post.setId((long) i);
            post.setTitle("帖子标题 " + i);
            // 为了让摘要测试更有意义，可以给一些帖子更长的内容
            if (i % 3 == 0) {
                post.setContent("这是帖子 " + i + " 的详细内容，它会比较长，用来测试摘要的截断功能是否正常工作，我们需要确保这里的文字数量超过两百个字符的限制。#Markdown标题 " + "重复一些文本 ".repeat(20) + " ![图片](url.jpg)");
            } else {
                post.setContent("这是帖子 " + i + " 的内容。");
            }
            post.setAuthor(testUser);
            post.setCategory(testCategory);
            post.setViewCount(10 + i);
            // 为了测试排序，让创建时间有所不同
            post.setCreatedAt(LocalDateTime.now().minusHours(12 - i)); // 最近的帖子是 Post 11
            postList.add(post);
        }

        //postList = Arrays.asList(testPost1, testPost2);
    }

    @Nested
    @DisplayName("findAll(Pageable pageable)")
    class FindAllPaginationTests {

        @Test
        @DisplayName("当有帖子时，返回正确的 PostDTO 分页数据")
        void findAll_whenPostsExist_shouldReturnPagedPostDTOs() {
            Pageable pageable = PageRequest.of(0, 5, Sort.by("createdAt").descending());
            Page<Post> mockPostPage = new PageImpl<>(postList, pageable, postList.size());

            when(postRepository.findAll(pageable)).thenReturn(mockPostPage);

            Page<PostDTO> result = postService.findAll(pageable);

            assertNotNull(result);
            assertEquals(2, result.getTotalElements(), "总元素数量应为2");
            assertEquals(1, result.getTotalPages(), "总页数应为1");
            assertEquals(2, result.getContent().size(), "当前页的帖子数量应为2");
            assertEquals(testPost1.getTitle(), result.getContent().get(0).getTitle(), "第一个帖子的标题应匹配");
            assertEquals(testPost2.getTitle(), result.getContent().get(1).getTitle(), "第二个帖子的标题应匹配");
            assertTrue(result.getContent().get(1).getSummaryContent().endsWith("..."), "第二个帖子的摘要应被截断");

            verify(postRepository).findAll(eq(pageable)); // 验证传入的 pageable 对象是否一致
        }

        @Test
        @DisplayName("当没有帖子时，返回空的 PostDTO 分页数据")
        void findAll_whenNoPostsExist_shouldReturnEmptyPage() {
            Pageable pageable = PageRequest.of(0, 5);
            Page<Post> emptyPostPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(postRepository.findAll(pageable)).thenReturn(emptyPostPage);

            Page<PostDTO> result = postService.findAll(pageable);

            assertNotNull(result);
            assertTrue(result.isEmpty(), "结果页面应为空");
            assertEquals(0, result.getTotalElements(), "总元素数量应为0");
            assertEquals(0, result.getContent().size(), "当前页的帖子数量应为0"); // 注意：PageImpl 在 total=0 时 getTotalPages 会是1，如果希望是0，需要调整 PageImpl 构造或断言
            assertEquals(1, result.getTotalPages(), "空数据时，PageImpl 默认总页数为1，除非总元素也为0且构造时明确");


            verify(postRepository).findAll(pageable);
        }

        @Test
        @DisplayName("请求特定页码和大小，返回相应的分页数据")
        void findAll_withSpecificPageAndSize_shouldReturnCorrectSlice() {
            // 假设有25个帖子，每页10个，请求第1页 (索引从0开始)
            List<Post> twentyFivePosts = new java.util.ArrayList<>();
            for(int i=1; i<=25; i++) {
                Post p = new Post(); p.setId((long)i); p.setTitle("Post " + i); p.setContent("Content " +i);
                p.setAuthor(testUser); p.setCategory(testCategory); p.setCreatedAt(LocalDateTime.now());
                twentyFivePosts.add(p);
            }

            Pageable pageableRequested = PageRequest.of(1, 10); // 请求第二页，每页10条
            // 模拟仓库返回第二页的数据
            List<Post> postsForPage1 = twentyFivePosts.subList(10, 20);
            Page<Post> mockPostPage = new PageImpl<>(postsForPage1, pageableRequested, twentyFivePosts.size());

            when(postRepository.findAll(pageableRequested)).thenReturn(mockPostPage);

            Page<PostDTO> result = postService.findAll(pageableRequested);

            assertNotNull(result);
            assertEquals(25, result.getTotalElements(), "总元素数量应为25");
            assertEquals(3, result.getTotalPages(), "总页数应为3");
            assertEquals(10, result.getContent().size(), "当前页的帖子数量应为10");
            assertEquals(1, result.getNumber(), "当前页码应为1"); // 页码从0开始
            assertEquals("Post 11", result.getContent().get(0).getTitle(), "第二页的第一个帖子标题应为 Post 11");

            verify(postRepository).findAll(pageableRequested);
        }
    }

    @Nested
    @DisplayName("findByCategoryId(Long categoryId, Pageable pageable)")
    class FindByCategoryIdPaginationTests {
        @Test
        @DisplayName("当指定分类下有帖子时，返回正确的 PostDTO 分页数据")
        void findByCategoryId_whenPostsExist_shouldReturnPagedPostDTOs() {
            Long categoryId = testCategory.getId();
            Pageable pageable = PageRequest.of(0, 5);
            Page<Post> mockPostPage = new PageImpl<>(postList, pageable, postList.size());

            when(postRepository.findByCategoryId(categoryId, pageable)).thenReturn(mockPostPage);

            Page<PostDTO> result = postService.findByCategoryId(categoryId, pageable);

            assertNotNull(result);
            assertEquals(2, result.getTotalElements());
            assertEquals(testPost1.getTitle(), result.getContent().get(0).getTitle());
            // 可以进一步验证 DTO 的内容
            assertEquals(testCategory.getName(), result.getContent().get(0).getCategory().getName());

            verify(postRepository).findByCategoryId(eq(categoryId), eq(pageable));
        }

        @Test
        @DisplayName("当指定分类下没有帖子时，返回空的 PostDTO 分页数据")
        void findByCategoryId_whenNoPostsExist_shouldReturnEmptyPage() {
            Long categoryId = 2L; // 一个不存在或没有帖子的分类ID
            Pageable pageable = PageRequest.of(0, 5);
            Page<Post> emptyPostPage = Page.empty(pageable); // 使用 Page.empty() 更简洁

            when(postRepository.findByCategoryId(categoryId, pageable)).thenReturn(emptyPostPage);

            Page<PostDTO> result = postService.findByCategoryId(categoryId, pageable);

            assertNotNull(result);
            assertTrue(result.isEmpty());
            assertEquals(0, result.getTotalElements());

            verify(postRepository).findByCategoryId(categoryId, pageable);
        }
    }

    @Nested
    @DisplayName("search(String keyword, Pageable pageable)")
    class SearchPaginationTests {
        @Test
        @DisplayName("当搜索到匹配帖子时，返回正确的 PostDTO 分页数据")
        void search_whenMatchesFound_shouldReturnPagedPostDTOs() {
            String keyword = "帖子1";
            Pageable pageable = PageRequest.of(0, 5);
            // 假设只有 testPost1 匹配
            Page<Post> mockPostPage = new PageImpl<>(Collections.singletonList(testPost1), pageable, 1);

            when(postRepository.findByTitleContainingOrContentContaining(keyword, keyword, pageable)).thenReturn(mockPostPage);

            Page<PostDTO> result = postService.search(keyword, pageable);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(testPost1.getTitle(), result.getContent().get(0).getTitle());

            verify(postRepository).findByTitleContainingOrContentContaining(eq(keyword), eq(keyword), eq(pageable));
        }

        @Test
        @DisplayName("当没有搜索到匹配帖子时，返回空的 PostDTO 分页数据")
        void search_whenNoMatchesFound_shouldReturnEmptyPage() {
            String keyword = "不存在的关键词";
            Pageable pageable = PageRequest.of(0, 5);
            Page<Post> emptyPostPage = Page.empty(pageable);

            when(postRepository.findByTitleContainingOrContentContaining(keyword, keyword, pageable)).thenReturn(emptyPostPage);

            Page<PostDTO> result = postService.search(keyword, pageable);

            assertNotNull(result);
            assertTrue(result.isEmpty());

            verify(postRepository).findByTitleContainingOrContentContaining(keyword, keyword, pageable);
        }
    }


    @Nested
    @DisplayName("findByAuthor(Long userId, Pageable pageable)")
    class FindByAuthorPaginationTests {
        @Test
        @DisplayName("当作者存在且有帖子时，返回正确的 PostDTO 分页数据")
        void findByAuthor_whenAuthorAndPostsExist_shouldReturnPagedPostDTOs() {
            Long authorId = testUser.getId();
            // 请求该作者帖子的第一页，每页5条，按创建时间降序
            //第一种写法：
            //for (int i = 0; i < 3; i++) {
            //    Pageable pageable = PageRequest.of(i, 5, Sort.by("createdAt").descending());
            //
            //    // 1. 准备该作者的所有帖子 (假设 allPosts 中所有帖子都属于 testUser，如果不是，你需要先筛选)
            //    //    在我们的 setUp 中，allPosts (11条) 的作者都是 testUser
            //    //List<Post> allPostsByAuthor = new ArrayList<>(postList); // 复制一份以防修改
            //
            //    // 2. 根据 pageable 参数，从 allPostsByAuthor 中提取出期望在当前页显示的帖子
            //    //    这里需要模拟数据库的分页和排序行为
            //    List<Post> expectedPageContent = postList.stream()
            //            .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt())) // 模拟排序
            //            .skip(pageable.getOffset()) // 跳过前面的页
            //            .limit(pageable.getPageSize()) // 取当前页的数量
            //            .collect(Collectors.toList());
            //
            //    for(Post post : expectedPageContent){
            //        System.out.println(post);
            //    }
            //}

            ////第一种写法的变式（Java 8 的 IntStream ）
            //IntStream.range(0, 3).forEach(i -> { // i 会依次是 0, 1
            //    Pageable pageable = PageRequest.of(i, 5, Sort.by("createdAt").descending());
            //
            //    List<Post> expectedPageContent = postList.stream()
            //            .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt())) // 模拟排序
            //            .skip(pageable.getOffset()) // 跳过前面的页的元素数量
            //            .limit(pageable.getPageSize()) // 取当前页的元素数量
            //            .collect(Collectors.toList());
            //
            //    System.out.println("--- 页码: " + i + " (每页 " + pageable.getPageSize() + " 条) ---");
            //    expectedPageContent.forEach(System.out::println); // 打印当前页的每个帖子
            //});

            //第二种方式


// 主逻辑
            for (int i = 0; i < 3; i++) {
                Pageable pageable = PageRequest.of(i, 5, Sort.by("createdAt").descending());
                List<Post> expectedPageContent = getPageContent(postList, pageable);

                System.out.println("--- 页码: " + i + " ---");
                expectedPageContent.forEach(System.out::println);
            }

            // 3. 构造模拟的 Page<Post> 对象
            //    - expectedPageContent 是当前页的内容 (例如，应该只有5条)
            //    - pageable 是请求的分页参数
            //    - allPostsByAuthor.size() 是该作者的总帖子数 (例如，11条)
            //Page<Post> mockPostPage = new PageImpl<>(expectedPageContent, pageable, postList.size());
            //
            //// 4. 模拟仓库和用户查找行为
            //when(userRepository.findById(authorId)).thenReturn(Optional.of(testUser));
            //// 当调用 postRepository.findByAuthor 时，返回我们精心构造的 mockPostPage
            //when(postRepository.findByAuthor(eq(testUser), eq(pageable))).thenReturn(mockPostPage);
            //
            //// 5. 执行服务方法
            //Page<PostDTO> result = postService.findByAuthor(authorId, pageable);
            //
            //// 6. 断言
            //assertNotNull(result);
            //assertEquals(postList.size(), result.getTotalElements(), "作者的总帖子数应为11"); // 总数是11
            //assertEquals(5, result.getContent().size(), "当前页的帖子数量应为5"); // 当前页内容是5条
            //// (11条数据，每页5条，总页数应该是3)
            //assertEquals(3, result.getTotalPages(), "总页数应为3");
            //assertEquals(0, result.getNumber(), "当前页码应为0"); // 页码从0开始
            //
            //if (!result.getContent().isEmpty()) {
            //    assertEquals(testUser.getUsername(), result.getContent().get(0).getAuthor().getUsername());
            //    // 验证返回的第一条数据是否符合排序和分页预期
            //    assertEquals("帖子标题 11", result.getContent().get(0).getTitle()); // 假设帖子11是最新的
            //}
            //
            //// 7. 验证 Mock 调用
            //verify(userRepository).findById(authorId);
            //verify(postRepository).findByAuthor(eq(testUser), eq(pageable));
            //Long authorId = testUser.getId();
            //Pageable pageable = PageRequest.of(0, 5);
            //Page<Post> mockPostPage = new PageImpl<>(postList, pageable, postList.size());
            //
            //when(userRepository.findById(authorId)).thenReturn(Optional.of(testUser));
            //when(postRepository.findByAuthor(testUser, pageable)).thenReturn(mockPostPage);
            //
            //Page<PostDTO> result = postService.findByAuthor(authorId, pageable);
            //
            //assertNotNull(result);
            //assertEquals(2, result.getTotalElements());
            //assertEquals(testUser.getUsername(), result.getContent().get(0).getAuthor().getUsername());
            //
            //verify(userRepository).findById(authorId);
            //verify(postRepository).findByAuthor(testUser, pageable);
        }
        // 辅助方法，用于从完整列表中获取特定页的内容
        private static List<Post> getPageContent(List<Post> fullList, Pageable pageable) {
            return fullList.stream()
                    .sorted((p1, p2) -> { // 假设排序逻辑基于 createdAt 降序
                        // 如果 pageable 中有排序信息，应该使用 pageable.getSort() 来动态构建 Comparator
                        // 这里为了简化，直接用了 createdAt 降序
                        Sort sort = pageable.getSort();
                        if (sort.isSorted()) {
                            Sort.Order orderForCreatedAt = sort.getOrderFor("createdAt");
                            if (orderForCreatedAt != null) {
                                if (orderForCreatedAt.getDirection() == Sort.Direction.DESC) {
                                    return p2.getCreatedAt().compareTo(p1.getCreatedAt());
                                } else {
                                    return p1.getCreatedAt().compareTo(p2.getCreatedAt());
                                }
                            }
                        }
                        // 默认排序或无排序时的回退（或抛出异常，表示pageable必须含排序）
                        return p2.getCreatedAt().compareTo(p1.getCreatedAt()); // 默认降序
                    })
                    .skip(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .collect(Collectors.toList());
        }

        @Test
        @DisplayName("当作者不存在时，抛出 ResourceNotFoundException")
        void findByAuthor_whenAuthorNotFound_shouldThrowResourceNotFoundException() {
            Long nonExistentAuthorId = 99L;
            Pageable pageable = PageRequest.of(0, 5);

            when(userRepository.findById(nonExistentAuthorId)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                postService.findByAuthor(nonExistentAuthorId, pageable);
            });

            assertEquals("用户不存在", exception.getMessage());
            verify(userRepository).findById(nonExistentAuthorId);
            verify(postRepository, never()).findByAuthor(any(User.class), any(Pageable.class));
        }

        @Test
        @DisplayName("当作者存在但没有帖子时，返回空的 PostDTO 分页数据")
        void findByAuthor_whenAuthorExistsButNoPosts_shouldReturnEmptyPage() {
            Long authorId = testUser.getId();
            Pageable pageable = PageRequest.of(0, 5);
            Page<Post> emptyPostPage = Page.empty(pageable);

            when(userRepository.findById(authorId)).thenReturn(Optional.of(testUser));
            when(postRepository.findByAuthor(testUser, pageable)).thenReturn(emptyPostPage);

            Page<PostDTO> result = postService.findByAuthor(authorId, pageable);

            assertNotNull(result);
            assertTrue(result.isEmpty());

            verify(userRepository).findById(authorId);
            verify(postRepository).findByAuthor(testUser, pageable);
        }
    }
}