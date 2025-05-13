package com.yqh.forum.controller;

import com.yqh.forum.dto.CommentDTO;
import com.yqh.forum.dto.PostDTO;
import com.yqh.forum.model.Post;
import com.yqh.forum.model.User; // **新增导入 User 实体类**
import com.yqh.forum.repository.UserRepository; // **新增导入 UserRepository**
import com.yqh.forum.service.CategoryService;
import com.yqh.forum.service.CommentService;
import com.yqh.forum.service.PostService;
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


    // List all posts or filter by category
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

        return "post/list"; // 返回 list.html 模板
    }

    // **处理 /post/my 请求，显示当前登录用户的帖子列表**
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

    //get
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("post", new PostDTO());
        model.addAttribute("categories", categoryService.findAll());
        return "post/create";
    }

    //post
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

    @GetMapping("/{id}")
    public String viewPost(@PathVariable Long id, Model model) {
        postService.incrementViewCount(id);

        PostDTO post = postService.findById(id);

        if (post == null) {
            return "error/404";
        }

        String markdownContent = post.getContent();
        // 请确保 MarkdownUtil 及其 convertMarkdownToHtml 方法已正确实现且可用
        String htmlContent = com.yqh.forum.service.util.MarkdownUtil.convertMarkdownToHtml(markdownContent); // Assuming package path

        model.addAttribute("post", post);
        model.addAttribute("postHtmlContent", htmlContent);

        Pageable pageable = PageRequest.of(0, 10);
        Page<CommentDTO> commentPage = commentService.findByPostId(id, pageable);

        model.addAttribute("comments", commentPage.getContent());

        return "post/view";
    }

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

    //将/{id}/edit映射到updatePost方法 /1/edit->redirect:/post/1
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

//package com.yqh.forum.controller;
//
//import com.yqh.forum.dto.CommentDTO;
//import com.yqh.forum.dto.PostDTO;
//import com.yqh.forum.model.Post;
//import com.yqh.forum.model.User;
//import com.yqh.forum.repository.UserRepository;
//import com.yqh.forum.service.CategoryService;
//import com.yqh.forum.service.CommentService;
//import com.yqh.forum.service.PostService;
//import com.yqh.forum.service.util.MarkdownUtil; // 新增导入 MarkdownUtil
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model; // **新增导入 Model**
//import org.springframework.validation.BindingResult;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import javax.validation.Valid;
//
//@Controller
//@RequestMapping("/post")
//public class PostController {
//
//    @Autowired
//    private PostService postService;
//
//    @Autowired
//    private CategoryService categoryService;
//
//    @Autowired // 注入 CommentService
//    private CommentService commentService;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @GetMapping
//    public String listPosts(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(required = false) Long categoryId,
//            Model model) {
//        PageRequest pageRequest = PageRequest.of(page, 10, Sort.by("createdAt").descending());
//        Page<PostDTO> posts;
//
//        if (categoryId != null) {
//            posts = postService.findByCategoryId(categoryId, pageRequest);
//        } else {
//            posts = postService.findAll(pageRequest);
//        }
//
//        model.addAttribute("posts", posts);
//        model.addAttribute("categories", categoryService.findAll());
//        model.addAttribute("categoryId", categoryId);
//        return "post/list";
//    }
//
//    @GetMapping("/search")
//    public String searchPosts(
//            @RequestParam String keyword,
//            @RequestParam(defaultValue = "0") int page,
//            Model model) {
//        PageRequest pageRequest = PageRequest.of(page, 10, Sort.by("createdAt").descending());
//        Page<PostDTO> posts = postService.search(keyword, pageRequest);
//
//        model.addAttribute("posts", posts);
//        model.addAttribute("keyword", keyword);
//        model.addAttribute("categories", categoryService.findAll());
//        return "post/list";
//    }
//
//    @GetMapping("/create")
//    public String showCreateForm(Model model) {
//        model.addAttribute("post", new PostDTO());
//        model.addAttribute("categories", categoryService.findAll());
//        return "post/create";
//    }
//
//    @PostMapping("/create")
//    public String createPost(
//            @Valid @ModelAttribute("post") PostDTO postDTO,
//            BindingResult result,
//            Model model, // **新增 Model 参数**
//            @AuthenticationPrincipal UserDetails userDetails,
//            RedirectAttributes redirectAttributes) {
//        if (result.hasErrors()) {
//            // 如果有错误，重新获取分类，以便在表单中显示
//            model.addAttribute("categories", categoryService.findAll());
//            return "post/create";
//        }
//
//        try {
//            postService.createPost(postDTO, userDetails.getUsername());
//            redirectAttributes.addFlashAttribute("successMessage", "帖子发布成功！"); // 保持与 base.html 一致的 flash 属性名
//            return "redirect:/post";
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "帖子发布失败：" + e.getMessage()); // 保持一致
//            return "redirect:/post/create";
//        }
//    }
//
//    //   显示帖子
//    @GetMapping("/{id}")
//    public String viewPost(@PathVariable Long id, Model model) {
//        //        增加浏览次数
//        postService.incrementViewCount(id);
//
//        //        通过postService先找到post
//        PostDTO post = postService.findById(id);
//
//        if (post == null) {
//            // 帖子不存在，返回 404 或错误页面
//            return "error/404"; // 假设您有 error/404.html 模板
//        }
//
//        // **新增：将 Markdown 内容转换成 HTML**
//        String markdownContent = post.getContent(); // 获取原始 Markdown 内容
//        String htmlContent = MarkdownUtil.convertMarkdownToHtml(markdownContent); // 使用工具类进行转换
//
//        //        添加到model中的变量
//        model.addAttribute("post", post);
//        // **新增：将转换后的 HTML 内容添加到 Model 中**
//        model.addAttribute("postHtmlContent", htmlContent);
//
//
//        //    我们先获取第一页评论，每页显示假设为 10 条
//        Pageable pageable = PageRequest.of(0, 10); // 获取第 0 页（即第一页），每页 10 条
//        Page<CommentDTO> commentPage = commentService.findByPostId(id, pageable);
//
//        model.addAttribute("comments", commentPage.getContent());
//
//        // **可选：如果评论内容也支持 Markdown，可以在这里进行转换**
//        // For (CommentDTO comment : commentPage.getContent()) {
//        //     comment.setHtmlContent(MarkdownUtil.convertMarkdownToHtml(comment.getContent()));
//        // }
//        // 然后在 view.html 中使用 th:utext="${comment.htmlContent}" 显示评论内容
//
//        return "post/view";
//    }
//
//    //
//    @GetMapping("/{id}/edit")
//    public String showEditForm(
//            @PathVariable Long id,
//            @AuthenticationPrincipal UserDetails userDetails,
//            Model model, // **新增 Model 参数**
//            RedirectAttributes redirectAttributes) {
//        PostDTO post = postService.findById(id);
//
//        if (post == null) {
//            return "error/404";
//        }
//
//        if (!post.getAuthor().getUsername().equals(userDetails.getUsername())) {
//            redirectAttributes.addFlashAttribute("errorMessage", "您没有权限编辑此帖子！"); // 保持一致
//            return "redirect:/post/" + id;
//        }
//
//        model.addAttribute("post", post);
//        model.addAttribute("categories", categoryService.findAll());
//        return "post/edit";
//    }
//
//    //    编辑时
//    @PostMapping("/{id}/edit")
//    public String updatePost(
//            @PathVariable Long id,
//            @Valid @ModelAttribute("post") PostDTO postDTO,
//            BindingResult result,
//            Model model, // **新增 Model 参数**
//            @AuthenticationPrincipal UserDetails userDetails,
//            RedirectAttributes redirectAttributes) {
//        if (result.hasErrors()) {
//            model.addAttribute("categories", categoryService.findAll());
//            return "post/edit";
//        }
//
//        try {
//            postService.updatePost(id, postDTO, userDetails.getUsername());
//            redirectAttributes.addFlashAttribute("successMessage", "帖子更新成功！"); // 保持一致
//            return "redirect:/post/" + id;
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "帖子更新失败：" + e.getMessage()); // 保持一致
//            return "redirect:/post/" + id + "/edit";
//        }
//    }
//
//    //    帖子删除操作
//    @PostMapping("/{id}/delete")
//    public String deletePost(
//            @PathVariable Long id,
//            @AuthenticationPrincipal UserDetails userDetails,
//            RedirectAttributes redirectAttributes) {
//        try {
//            postService.deletePost(id, userDetails.getUsername());
//            redirectAttributes.addFlashAttribute("successMessage", "帖子删除成功！"); // 保持一致
//            return "redirect:/post";
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "帖子删除失败：" + e.getMessage()); // 保持一致
//            return "redirect:/post/" + id;
//        }
//    }
//
//    // **新增：处理 /post/my 请求，显示当前登录用户的帖子列表**
//    @GetMapping("/my")
//    public String listMyPosts(
//            @RequestParam(defaultValue = "0") int page, // 分页参数
//            @AuthenticationPrincipal UserDetails userDetails, // 获取当前登录用户详情
//            Model model) {
//
//        // 确保用户已认证（Spring Security 通常会处理未认证用户的访问）
//        if (userDetails == null) {
//            // 如果用户未登录，可以重定向到登录页或显示错误
//            return "redirect:/login";
//        }
//
//        // 根据用户名查找用户实体，获取用户 ID
//        User currentUser = userRepository.findByUsername(userDetails.getUsername())
//                // 如果找不到用户，可能是数据不一致或其他问题，抛出异常
//                .orElseThrow(() -> new RuntimeException("Logged-in user not found in database"));
//
//        // 根据当前用户 ID 获取该用户的帖子列表 (分页)
//        PageRequest pageRequest = PageRequest.of(page, 10, Sort.by("createdAt").descending());
//        Page<PostDTO> posts = postService.findByAuthor(currentUser.getId(), pageRequest);
//
//        model.addAttribute("posts", posts); // 将帖子列表添加到 Model
//        model.addAttribute("categories", categoryService.findAll()); // 添加分类列表 (用于侧边栏)
//        // **新增：标识当前是“我的帖子”视图**
//        model.addAttribute("isMyPostsView", true);
//
//        return "post/list"; // 复用 list.html 模板来显示结果
//    }
//}