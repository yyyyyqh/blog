package com.yqh.forum.service;

import com.yqh.forum.dto.CategoryDTO;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching; // 导入 Caching 注解
import java.util.List;

public interface CategoryService {

    // 创建分类后，清除所有分类列表的缓存
    @CacheEvict(value = "categories", allEntries = true)
    CategoryDTO createCategory(CategoryDTO categoryDTO);

    // 更新分类后，清除该分类的缓存以及所有分类列表的缓存
    @Caching(evict = {
            @CacheEvict(value = "category", key = "#id"), // 清除单个分类缓存
            @CacheEvict(value = "categories", allEntries = true) // 清除所有分类列表缓存
    })
    CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO);

    // 删除分类后，清除该分类的缓存以及所有分类列表的缓存
    @Caching(evict = {
            @CacheEvict(value = "category", key = "#id"), // 清除单个分类缓存
            @CacheEvict(value = "categories", allEntries = true) // 清除所有分类列表缓存
    })
    void deleteCategory(Long id);

    // 缓存单个分类详情
    @Cacheable(value = "category", key = "#id")
    CategoryDTO findById(Long id);

    // 缓存所有分类列表
    @Cacheable(value = "categories") // 对于无参数的 findAll，可以只指定 value
    List<CategoryDTO> findAll();

    // 这个方法通常不缓存，因为它用于实时校验
    boolean existsByName(String name);
}

//package com.yqh.forum.service;
//
//import com.yqh.forum.dto.CategoryDTO;
//import java.util.List;
//
//public interface CategoryService {
//    CategoryDTO createCategory(CategoryDTO categoryDTO);
//    CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO);
//    void deleteCategory(Long id);
//    CategoryDTO findById(Long id);
//    List<CategoryDTO> findAll();
//    boolean existsByName(String name);
//}