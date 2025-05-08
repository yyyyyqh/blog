package com.yqh.forum.service;

import com.yqh.forum.dto.CategoryDTO;
import java.util.List;

public interface CategoryService {
    CategoryDTO createCategory(CategoryDTO categoryDTO);
    CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO);
    void deleteCategory(Long id);
    CategoryDTO findById(Long id);
    List<CategoryDTO> findAll();
    boolean existsByName(String name);
} 