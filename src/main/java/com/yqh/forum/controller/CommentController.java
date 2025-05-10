package com.yqh.forum.controller;

import com.yqh.forum.dto.CommentDTO;
import com.yqh.forum.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

//    评论发布时的处理
    @PostMapping("/create")
    public String createComment(@ModelAttribute CommentDTO commentDTO,
                              RedirectAttributes redirectAttributes) {
        try {
            commentService.createComment(commentDTO);
            redirectAttributes.addFlashAttribute("successMessage", "评论发布成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "评论发布失败：" + e.getMessage());
        }
//    返回了一个重定向地址    在header的Location属性，重定向的位置
        return "redirect:/post/" + commentDTO.getPostId();
    }

    @PostMapping("/{id}/edit")
    public String updateComment(@PathVariable Long id,
                              @ModelAttribute CommentDTO commentDTO,
                              RedirectAttributes redirectAttributes) {
        try {
            commentService.updateComment(id, commentDTO);
            redirectAttributes.addFlashAttribute("successMessage", "评论更新成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "评论更新失败：" + e.getMessage());
        }
        return "redirect:/post/" + commentDTO.getPostId();
    }

    @PostMapping("/{id}/delete")
    public String deleteComment(@PathVariable Long id,
                              @RequestParam Long postId,
                              RedirectAttributes redirectAttributes) {
        try {
            commentService.deleteComment(id);
            redirectAttributes.addFlashAttribute("successMessage", "评论删除成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "评论删除失败：" + e.getMessage());
        }
        return "redirect:/post/" + postId;
    }

    @GetMapping("/post/{postId}")
    public String listComments(@PathVariable Long postId,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size) {
        Page<CommentDTO> comments = commentService.findByPostId(postId,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return "comment/list";
    }
} 