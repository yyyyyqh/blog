package com.yqh.forum.controller;

import com.yqh.forum.dto.UserDTO;
import com.yqh.forum.dto.UserRegistrationDTO;
import com.yqh.forum.model.User;
import com.yqh.forum.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/avatars/";

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationDTO());
        return "user/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDTO registrationDTO,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "user/register";
        }

        try {
            userService.registerNewUser(registrationDTO);
            redirectAttributes.addFlashAttribute("successMessage", "注册成功，请登录");
            return "redirect:/user/login";
        } catch (RuntimeException e) {
            result.rejectValue("username", "error.user", e.getMessage());
            return "user/register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "user/login";
    }

    @GetMapping("/profile")
    public String showProfile(Model model) {
        UserDTO userDTO = userService.findByUsername(userService.getCurrentUser().getUsername());
        model.addAttribute("user", userDTO);
        return "user/profile";
    }

    @GetMapping("/profile/edit")
    public String showEditProfileForm(Model model) {
        UserDTO userDTO = userService.findByUsername(userService.getCurrentUser().getUsername());
        model.addAttribute("user", userDTO);
        return "user/edit-profile";
    }

    @PostMapping("/profile/edit")
    public String updateProfile(@ModelAttribute UserDTO userDTO,
                              @RequestParam("avatarFile") MultipartFile avatarFile,
                              RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.getCurrentUser();
            
            // 处理头像上传
            if (avatarFile != null && !avatarFile.isEmpty()) {
//                生成文件名逻辑
                String fileName = currentUser.getId() + "_" + System.currentTimeMillis() + 
                                avatarFile.getOriginalFilename().substring(avatarFile.getOriginalFilename().lastIndexOf("."));
                
                // 确保上传目录存在
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                
                // 保存文件
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(avatarFile.getInputStream(), filePath);
                
                // 更新用户头像URL
                currentUser.setAvatar("/uploads/avatars/" + fileName);
            }
            
            // 更新其他信息
            currentUser.setEmail(userDTO.getEmail());
            userService.updateUser(currentUser);
            
            redirectAttributes.addFlashAttribute("successMessage", "个人资料更新成功");
            return "redirect:/user/profile";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "头像上传失败：" + e.getMessage());
            return "redirect:/user/profile/edit";
        }
    }

    @GetMapping("/profile/password")
    public String showChangePasswordForm() {
        return "user/change-password";
    }

    @PostMapping("/profile/password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                               @RequestParam("newPassword") String newPassword,
                               @RequestParam("confirmPassword") String confirmPassword,
                               RedirectAttributes redirectAttributes) {
        try {
            userService.changePassword(currentPassword, newPassword, confirmPassword);
            redirectAttributes.addFlashAttribute("successMessage", "密码修改成功");
            return "redirect:/user/profile";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/user/profile/password";
        }
    }
} 