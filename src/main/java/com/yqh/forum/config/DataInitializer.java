package com.yqh.forum.config;

import com.yqh.forum.model.Category;
import com.yqh.forum.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        // 检查是否已经有分类数据
        if (categoryRepository.count() == 0) {
            // 创建默认分类
            Category general = new Category();
            general.setName("综合讨论");
            general.setDescription("一般性话题讨论");
            categoryRepository.save(general);

            Category tech = new Category();
            tech.setName("技术交流");
            tech.setDescription("技术相关话题讨论");
            categoryRepository.save(tech);

            Category life = new Category();
            life.setName("生活分享");
            life.setDescription("生活相关话题讨论");
            categoryRepository.save(life);

            Category news = new Category();
            news.setName("新闻资讯");
            news.setDescription("新闻相关话题讨论");
            categoryRepository.save(news);
        }
    }
} 