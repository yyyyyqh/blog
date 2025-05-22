package com.yqh.forum.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 主页重定向控制器
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/post";
    }
} 