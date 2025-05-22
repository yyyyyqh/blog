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

    /**
     * 发表评论
     * @param commentDTO
     * @param redirectAttributes
     * @return "redirect:/post/" + commentDTO.getPostId()
     */
    @PostMapping("/create")
    public String createComment(@ModelAttribute CommentDTO commentDTO,
                              RedirectAttributes redirectAttributes) {
        try {
            commentService.createComment(commentDTO);
            redirectAttributes.addFlashAttribute("successMessage", "评论发布成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "评论发布失败：" + e.getMessage());
        }
        return "redirect:/post/" + commentDTO.getPostId();
    }

    /**
     * 修改评论, /comment/{id}/edit
     * @param id
     * @param commentDTO
     * @param redirectAttributes
     * @return "redirect:/post/" + commentDTO.getPostId()
     */
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

    /**
     * 删除评论, /comment/{id}/delete
     * @param id
     * @param postId
     * @param redirectAttributes
     * @return "redirect:/post/" + postId
     */
    @PostMapping("/{id}/delete")
    public String deleteComment(@PathVariable Long id,
                              @RequestParam Long postId,
                              RedirectAttributes redirectAttributes) {
        try {
            commentService.deleteComment(id);
            //
            redirectAttributes.addFlashAttribute("successMessage", "评论删除成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "评论删除失败：" + e.getMessage());
        }
        return "redirect:/post/" + postId;
    }

    /**
     * 评论列表，集成在PostController中，这里的暂时不处理
     */
    //@GetMapping("/post/{postId}")
    //public String listComments(@PathVariable Long postId,
    //                         @RequestParam(defaultValue = "0") int page,
    //                         @RequestParam(defaultValue = "10") int size) {
    //    Page<CommentDTO> comments = commentService.findByPostId(postId,
    //            PageRequest.of(page, size, Sort.by("createdAt").descending()));
    //    return "comment/list";
    //}
} 