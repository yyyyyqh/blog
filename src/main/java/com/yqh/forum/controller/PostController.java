package com.yqh.forum.controller;

import com.yqh.forum.dto.CommentDTO;
import com.yqh.forum.dto.PostDTO;
import com.yqh.forum.model.Post;
import com.yqh.forum.service.CategoryService;
import com.yqh.forum.service.CommentService;
import com.yqh.forum.service.PostService;
import com.yqh.forum.service.util.MarkdownUtil; // 新增导入 MarkdownUtil

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // **新增导入 Model**
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

    @Autowired // 注入 CommentService
    private CommentService commentService;

    @GetMapping
    public String listPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Long categoryId,
            Model model) {
        PageRequest pageRequest = PageRequest.of(page, 10, Sort.by("createdAt").descending());
        Page<PostDTO> posts;

        if (categoryId != null) {
            posts = postService.findByCategoryId(categoryId, pageRequest);
        } else {
            posts = postService.findAll(pageRequest);
        }

        model.addAttribute("posts", posts);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("categoryId", categoryId);
        return "post/list";
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
        return "post/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("post", new PostDTO());
        model.addAttribute("categories", categoryService.findAll());
        return "post/create";
    }

    @PostMapping("/create")
    public String createPost(
            @Valid @ModelAttribute("post") PostDTO postDTO,
            BindingResult result,
            Model model, // **新增 Model 参数**
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            // 如果有错误，重新获取分类，以便在表单中显示
            model.addAttribute("categories", categoryService.findAll());
            return "post/create";
        }

        try {
            postService.createPost(postDTO, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "帖子发布成功！"); // 保持与 base.html 一致的 flash 属性名
            return "redirect:/post";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "帖子发布失败：" + e.getMessage()); // 保持一致
            return "redirect:/post/create";
        }
    }

    //   显示帖子
    @GetMapping("/{id}")
    public String viewPost(@PathVariable Long id, Model model) {
        //        增加浏览次数
        postService.incrementViewCount(id);

        //        通过postService先找到post
        PostDTO post = postService.findById(id);

        if (post == null) {
            // 帖子不存在，返回 404 或错误页面
            return "error/404"; // 假设您有 error/404.html 模板
        }

        // **新增：将 Markdown 内容转换成 HTML**
        String markdownContent = post.getContent(); // 获取原始 Markdown 内容
        String htmlContent = MarkdownUtil.convertMarkdownToHtml(markdownContent); // 使用工具类进行转换

        //        添加到model中的变量
        model.addAttribute("post", post);
        // **新增：将转换后的 HTML 内容添加到 Model 中**
        model.addAttribute("postHtmlContent", htmlContent);


        //    我们先获取第一页评论，每页显示假设为 10 条
        Pageable pageable = PageRequest.of(0, 10); // 获取第 0 页（即第一页），每页 10 条
        Page<CommentDTO> commentPage = commentService.findByPostId(id, pageable);

        model.addAttribute("comments", commentPage.getContent());

        // **可选：如果评论内容也支持 Markdown，可以在这里进行转换**
        // For (CommentDTO comment : commentPage.getContent()) {
        //     comment.setHtmlContent(MarkdownUtil.convertMarkdownToHtml(comment.getContent()));
        // }
        // 然后在 view.html 中使用 th:utext="${comment.htmlContent}" 显示评论内容

        return "post/view";
    }

    //
    @GetMapping("/{id}/edit")
    public String showEditForm(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model, // **新增 Model 参数**
            RedirectAttributes redirectAttributes) {
        PostDTO post = postService.findById(id);

        if (post == null) {
            return "error/404";
        }

        if (!post.getAuthor().getUsername().equals(userDetails.getUsername())) {
            redirectAttributes.addFlashAttribute("errorMessage", "您没有权限编辑此帖子！"); // 保持一致
            return "redirect:/post/" + id;
        }

        model.addAttribute("post", post);
        model.addAttribute("categories", categoryService.findAll());
        return "post/edit";
    }

    //    编辑时
    @PostMapping("/{id}/edit")
    public String updatePost(
            @PathVariable Long id,
            @Valid @ModelAttribute("post") PostDTO postDTO,
            BindingResult result,
            Model model, // **新增 Model 参数**
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            return "post/edit";
        }

        try {
            postService.updatePost(id, postDTO, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "帖子更新成功！"); // 保持一致
            return "redirect:/post/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "帖子更新失败：" + e.getMessage()); // 保持一致
            return "redirect:/post/" + id + "/edit";
        }
    }

    //    帖子删除操作
    @PostMapping("/{id}/delete")
    public String deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            postService.deletePost(id, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "帖子删除成功！"); // 保持一致
            return "redirect:/post";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "帖子删除失败：" + e.getMessage()); // 保持一致
            return "redirect:/post/" + id;
        }
    }
}