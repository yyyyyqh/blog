package com.yqh.forum.service.impl;

import com.yqh.forum.dto.CategoryDTO;
import com.yqh.forum.model.Category;
import com.yqh.forum.repository.CategoryRepository;
import com.yqh.forum.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService { // 确保实现了 CategoryService 接口

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    // CacheEvict 注解在接口上已经定义，此处无需重复
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        if (categoryRepository.existsByName(categoryDTO.getName())) {
            throw new IllegalArgumentException("分类名称已存在");
        }

        Category category = new Category();
        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());

        Category savedCategory = categoryRepository.save(category);
        return convertToDTO(savedCategory);
    }

    @Override
    // Caching 注解在接口上已经定义，此处无需重复
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("分类不存在"));

        if (!category.getName().equals(categoryDTO.getName()) &&
                categoryRepository.existsByName(categoryDTO.getName())) {
            throw new IllegalArgumentException("分类名称已存在");
        }

        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());

        Category updatedCategory = categoryRepository.save(category);
        return convertToDTO(updatedCategory);
    }

    @Override
    // Caching 注解在接口上已经定义，此处无需重复
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    @Override
    // Cacheable 注解在接口上已经定义，此处无需重复
    public CategoryDTO findById(Long id) {
        return categoryRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Override
    // Cacheable 注解在接口上已经定义，此处无需重复
    public List<CategoryDTO> findAll() {
        return categoryRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }

    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        return dto;
    }

}

//package com.yqh.forum.service.impl;
//
//import com.yqh.forum.dto.CategoryDTO;
//import com.yqh.forum.model.Category;
//import com.yqh.forum.repository.CategoryRepository;
//import com.yqh.forum.service.CategoryService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@Transactional
//public class CategoryServiceImpl implements CategoryService {
//
//    @Autowired
//    private CategoryRepository categoryRepository;
//
//    @Override
//    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
//        if (categoryRepository.existsByName(categoryDTO.getName())) {
//            throw new IllegalArgumentException("分类名称已存在");
//        }
//
//        Category category = new Category();
//        category.setName(categoryDTO.getName());
//        category.setDescription(categoryDTO.getDescription());
//
//        Category savedCategory = categoryRepository.save(category);
//        return convertToDTO(savedCategory);
//    }
//
//    @Override
//    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
//        Category category = categoryRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("分类不存在"));
//
//        if (!category.getName().equals(categoryDTO.getName()) &&
//            categoryRepository.existsByName(categoryDTO.getName())) {
//            throw new IllegalArgumentException("分类名称已存在");
//        }
//
//        category.setName(categoryDTO.getName());
//        category.setDescription(categoryDTO.getDescription());
//
//        Category updatedCategory = categoryRepository.save(category);
//        return convertToDTO(updatedCategory);
//    }
//
//    @Override
//    public void deleteCategory(Long id) {
//        categoryRepository.deleteById(id);
//    }
//
//    @Override
//    public CategoryDTO findById(Long id) {
//        return categoryRepository.findById(id)
//                .map(this::convertToDTO)
//                .orElse(null);
//    }
//
//    @Override
//    public List<CategoryDTO> findAll() {
//        return categoryRepository.findAll().stream()
//                .map(this::convertToDTO)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public boolean existsByName(String name) {
//        return categoryRepository.existsByName(name);
//    }
//
//    private CategoryDTO convertToDTO(Category category) {
//        CategoryDTO dto = new CategoryDTO();
//        dto.setId(category.getId());
//        dto.setName(category.getName());
//        dto.setDescription(category.getDescription());
//        dto.setCreatedAt(category.getCreatedAt());
//        dto.setUpdatedAt(category.getUpdatedAt());
//        return dto;
//    }
//
//}