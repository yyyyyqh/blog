package com.yqh.forum.controller;

import com.yqh.forum.dto.PostDTO;
import com.yqh.forum.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/post")
public class PostController {

    @Autowired
    private PostService postService;

    @GetMapping
    public String listPosts(@RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "10") int size,
                          Model model) {
        Page<PostDTO> posts = postService.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
        model.addAttribute("posts", posts);
        return "post/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("post", new PostDTO());
        return "post/create";
    }

    @PostMapping("/create")
    public String createPost(@ModelAttribute PostDTO postDTO,
                           RedirectAttributes redirectAttributes) {
        try {
            postService.createPost(postDTO);
            redirectAttributes.addFlashAttribute("successMessage", "发帖成功");
            return "redirect:/post";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "发帖失败：" + e.getMessage());
            return "redirect:/post/create";
        }
    }

    @GetMapping("/{id}")
    public String viewPost(@PathVariable Long id, Model model) {
        PostDTO post = postService.findById(id);
        if (post == null) {
            return "redirect:/post";
        }
        postService.incrementViewCount(id);
        model.addAttribute("post", post);
        return "post/view";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        PostDTO post = postService.findById(id);
        if (post == null) {
            return "redirect:/post";
        }
        model.addAttribute("post", post);
        return "post/edit";
    }

    @PostMapping("/{id}/edit")
    public String updatePost(@PathVariable Long id,
                           @ModelAttribute PostDTO postDTO,
                           RedirectAttributes redirectAttributes) {
        try {
            postService.updatePost(id, postDTO);
            redirectAttributes.addFlashAttribute("successMessage", "帖子更新成功");
            return "redirect:/post/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "更新失败：" + e.getMessage());
            return "redirect:/post/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deletePost(@PathVariable Long id,
                           RedirectAttributes redirectAttributes) {
        try {
            postService.deletePost(id);
            redirectAttributes.addFlashAttribute("successMessage", "帖子删除成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "删除失败：" + e.getMessage());
        }
        return "redirect:/post";
    }

    @GetMapping("/search")
    public String searchPosts(@RequestParam String keyword,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            Model model) {
        Page<PostDTO> posts = postService.search(keyword, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        model.addAttribute("posts", posts);
        model.addAttribute("keyword", keyword);
        return "post/search";
    }
} 