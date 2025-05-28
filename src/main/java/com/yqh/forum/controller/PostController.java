package com.yqh.forum.controller;

import com.yqh.forum.dto.CommentDTO;
import com.yqh.forum.dto.PostDTO;
import com.yqh.forum.model.Post;
import com.yqh.forum.model.User; // **新增导入 User 实体类**
import com.yqh.forum.repository.UserRepository; // **新增导入 UserRepository**
import com.yqh.forum.service.CategoryService;
import com.yqh.forum.service.CommentService;
import com.yqh.forum.service.PostService;
import com.yqh.forum.service.util.HtmlHeadingIdUtil;
import com.yqh.forum.service.util.MarkdownUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

/**
 * 帖子控制器
 */
@Controller
@RequestMapping("/post")
public class PostController {

    @Autowired
    private PostService postService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private CommentService commentService;
    @Autowired // **新增注入 UserRepository**
    private UserRepository userRepository;


    /**
     * 显示所有帖子，或根据分类显示帖子
     * @param page
     * @param categoryId
     * @param model
     * @return "post/list"
     */
    @GetMapping
    public String listPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Long categoryId,
            Model model) {
        PageRequest pageRequest = PageRequest.of(page, 10, Sort.by("createdAt").descending());
        Page<PostDTO> posts;

        if (categoryId != null) {
            posts = postService.findByCategoryId(categoryId, pageRequest);
            // 在 Model 中保留 categoryId，以便在分页链接中使用
            model.addAttribute("categoryId", categoryId);
        } else {
            posts = postService.findAll(pageRequest);
        }

        model.addAttribute("posts", posts);
        model.addAttribute("categories", categoryService.findAll());
        // **确保添加 isMyPostsView 属性**
        model.addAttribute("isMyPostsView", false);

        return "post/list";
    }

    /**
     * 显示当前登录用户的帖子列表
     * @param page
     * @param userDetails
     * @param model
     * @return "post/list"
     */
    @GetMapping("/my")
    public String listMyPosts(
            @RequestParam(defaultValue = "0") int page, // 分页参数
            @AuthenticationPrincipal UserDetails userDetails, // 获取当前登录用户详情
            Model model) {

        // 确保用户已认证
        if (userDetails == null) {
            return "redirect:/login";
        }

        // 根据用户名查找用户实体，获取用户 ID
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Logged-in user not found in database"));

        // 根据当前用户 ID 获取该用户的帖子列表 (分页)
        PageRequest pageRequest = PageRequest.of(page, 10, Sort.by("createdAt").descending());
        Page<PostDTO> posts = postService.findByAuthor(currentUser.getId(), pageRequest);

        model.addAttribute("posts", posts); // 将帖子列表添加到 Model
        model.addAttribute("categories", categoryService.findAll()); // 添加分类列表 (用于侧边栏)
        // **确保添加 isMyPostsView 属性**
        model.addAttribute("isMyPostsView", true);

        return "post/list"; // 复用 list.html 模板来显示结果
    }

    /**
     * 搜索帖子
     * @param keyword
     * @param page
     * @param model
     * @return "post/list"
     */
    @GetMapping("/search")
    public String searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        PageRequest pageRequest = PageRequest.of(page, 10, Sort.by("createdAt").descending());
        Page<PostDTO> posts = postService.search(keyword, pageRequest);

        model.addAttribute("posts", posts);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categories", categoryService.findAll());
        // **确保添加 isMyPostsView 属性**
        model.addAttribute("isMyPostsView", false);

        return "post/list";
    }

    /**
     * 发布帖子页面
     * @param model
     * @return
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("post", new PostDTO());
        model.addAttribute("categories", categoryService.findAll());
        return "post/create";
    }

    /**
     * 发送创建帖子请求
     * @param postDTO
     * @param result
     * @param model
     * @param userDetails
     * @param redirectAttributes
     * @return
     */
    @PostMapping("/create")
    public String createPost(
            @Valid @ModelAttribute("post") PostDTO postDTO,
            BindingResult result,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            return "post/create";
        }

        try {
            postService.createPost(postDTO, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "帖子发布成功！");
            return "redirect:/post";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "帖子发布失败：" + e.getMessage());
            return "redirect:/post/create";
        }
    }
    //

    /**
     * 帖子详情页面
     * @param id
     * @param model
     * @return "post/view"
     */
    @GetMapping("/{id}")
    public String viewPost(@PathVariable Long id, Model model) {
        postService.incrementViewCount(id);

        PostDTO post = postService.findById(id);

        if (post == null) {
            return "error/404";
        }


        //获取数据库中的markdown内容
        String markdownContent = post.getContent();
        //转换html
        String rawHtmlContent = com.yqh.forum.service.util.MarkdownUtil.convertMarkdownToHtml(markdownContent);
        //对html添加id
        String htmlContentWithIds = HtmlHeadingIdUtil.addIdsToHtmlHeadings(rawHtmlContent);

        System.out.println(htmlContentWithIds);
        model.addAttribute("post", post);
        model.addAttribute("postHtmlContent", htmlContentWithIds);

        //评论列表
        Pageable pageable = PageRequest.of(0, 10);
        Page<CommentDTO> commentPage = commentService.findByPostId(id, pageable);

        model.addAttribute("comments", commentPage.getContent());

        return "post/view";
    }

    /**
     * 帖子编辑页面
     * /post/{id}/edit
     * @return "post/edit"
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {
        PostDTO post = postService.findById(id);

        if (post == null) {
            return "error/404";
        }

        if (!post.getAuthor().getUsername().equals(userDetails.getUsername())) {
            redirectAttributes.addFlashAttribute("errorMessage", "您没有权限编辑此帖子！");
            return "redirect:/post/" + id;
        }

        model.addAttribute("post", post);
        model.addAttribute("categories", categoryService.findAll());
        return "post/edit";
    }

    //

    /**
     * post操作，编辑完成跳转回帖子
     * @param id
     * @param postDTO
     * @param result
     * @param model
     * @param userDetails
     * @param redirectAttributes
     * @return "post/edit"
     */
    @PostMapping("/{id}/edit")
    public String updatePost(
            @PathVariable Long id,
            @Valid @ModelAttribute("post") PostDTO postDTO,
            BindingResult result,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            return "post/edit";
        }

        try {
            postService.updatePost(id, postDTO, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "帖子更新成功！");
            return "redirect:/post/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "帖子更新失败：" + e.getMessage());
            return "redirect:/post/" + id + "/edit";
        }
    }
    //

    /**
     * 帖子删除操作
     *
     * @param id
     * @param userDetails
     * @param redirectAttributes
     * @return "redirect:/post/"
     */
    @PostMapping("/{id}/delete")
    public String deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            postService.deletePost(id, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "帖子删除成功！");
            return "redirect:/post";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "帖子删除失败：" + e.getMessage());
            return "redirect:/post/" + id;
        }
    }
}