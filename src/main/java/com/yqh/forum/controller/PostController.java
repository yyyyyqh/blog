package com.yqh.forum.controller;

import com.yqh.forum.dto.PostDTO;
import com.yqh.forum.model.Post;
import com.yqh.forum.service.CategoryService;
import com.yqh.forum.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "post/create";
        }

        try {
            postService.createPost(postDTO, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("success", "帖子发布成功！");
            return "redirect:/post";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "帖子发布失败：" + e.getMessage());
            return "redirect:/post/create";
        }
    }

    @GetMapping("/{id}")
    public String viewPost(@PathVariable Long id, Model model) {
        PostDTO post = postService.findById(id);
        model.addAttribute("post", post);
        return "post/view";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {
        PostDTO post = postService.findById(id);
        
        if (!post.getAuthor().getUsername().equals(userDetails.getUsername())) {
            redirectAttributes.addFlashAttribute("error", "您没有权限编辑此帖子！");
            return "redirect:/post/" + id;
        }
        
        model.addAttribute("post", post);
        model.addAttribute("categories", categoryService.findAll());
        return "post/edit";
    }

    @PostMapping("/{id}/edit")
    public String updatePost(
            @PathVariable Long id,
            @Valid @ModelAttribute("post") PostDTO postDTO,
            BindingResult result,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "post/edit";
        }

        try {
            postService.updatePost(id, postDTO, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("success", "帖子更新成功！");
            return "redirect:/post/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "帖子更新失败：" + e.getMessage());
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
            redirectAttributes.addFlashAttribute("success", "帖子删除成功！");
            return "redirect:/post";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "帖子删除失败：" + e.getMessage());
            return "redirect:/post/" + id;
        }
    }
} 