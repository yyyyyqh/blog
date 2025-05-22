package com.yqh.forum.controller;

import com.yqh.forum.dto.CategoryDTO;
import com.yqh.forum.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 分类控制器
 * （暂未使用到）
 */
@Controller
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "category/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("category", new CategoryDTO());
        return "category/create";
    }

    @PostMapping("/create")
    public String createCategory(@ModelAttribute CategoryDTO categoryDTO,
                               RedirectAttributes redirectAttributes) {
        try {
            categoryService.createCategory(categoryDTO);
            redirectAttributes.addFlashAttribute("successMessage", "分类创建成功");
            return "redirect:/category";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "分类创建失败：" + e.getMessage());
            return "redirect:/category/create";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        CategoryDTO category = categoryService.findById(id);
        if (category == null) {
            return "redirect:/category";
        }
        model.addAttribute("category", category);
        return "category/edit";
    }

    @PostMapping("/{id}/edit")
    public String updateCategory(@PathVariable Long id,
                               @ModelAttribute CategoryDTO categoryDTO,
                               RedirectAttributes redirectAttributes) {
        try {
            categoryService.updateCategory(id, categoryDTO);
            redirectAttributes.addFlashAttribute("successMessage", "分类更新成功");
            return "redirect:/category";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "分类更新失败：" + e.getMessage());
            return "redirect:/category/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteCategory(@PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "分类删除成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "分类删除失败：" + e.getMessage());
        }
        return "redirect:/category";
    }
} 