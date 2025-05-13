package com.yqh.forum.controller;

import com.yqh.forum.dto.UserDTO;
import com.yqh.forum.dto.UserRegistrationDTO;
import com.yqh.forum.model.User;
import com.yqh.forum.security.ForumUserDetails;
import com.yqh.forum.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collection;

@Controller
@RequestMapping("/user")
//@RequiredArgsConstructor 使用了 @Value 注入字段，通常不再需要 @RequiredArgsConstructor
public class UserController {

//    使用Autowired注入，@Value注入字段不能是final
    @Autowired
    private UserService userService;
//    待注入
    @Value("${app.avatar-upload-dir}")
    private String UPLOAD_DIR;
//    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/avatars/";

    // **注入 EntityManager**
    @PersistenceContext
    private EntityManager entityManager;

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
                              RedirectAttributes redirectAttributes,
                                @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = userService.getCurrentUser();
            
            // 处理头像上传
            if (avatarFile != null && !avatarFile.isEmpty()) {
                //生成文件名逻辑
                String fileName = currentUser.getId() + "_" + System.currentTimeMillis() + 
                                avatarFile.getOriginalFilename().substring(avatarFile.getOriginalFilename().lastIndexOf("."));
                
                // 确保上传目录存在
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
//                    createDirectories创建多级目录
                    Files.createDirectories(uploadPath);
                }
                
                // 保存文件
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(avatarFile.getInputStream(), filePath);
                
                // 更新用户头像URL
                currentUser.setAvatar("/uploads/avatars/" + fileName);
//                String webAccessiblePath = "/uploads/avatars/" + fileName;

                // ** 实现缓存穿透 (Cache Busting) **
                // 在基础路径后面添加一个唯一的时间戳作为查询参数
//                String cacheBustedPath = webAccessiblePath + "?v=" + Instant.now().toEpochMilli();
//                currentUser.setAvatar(cacheBustedPath);



            }
            // 更新其他信息
            currentUser.setEmail(userDTO.getEmail());
            //.

            //需要放在UserDetails对象更新之前
            userService.updateUser(currentUser);

//  更新${#authentication.principal.avatar}里的头像URL
            // 1. 从数据库重新获取最新的用户实体 (确保数据是最新的，包括头像 URL)
            //    或者确保 userService.updateUser 返回了更新后的实体并使用它
            User updatedUserEntity = userService.getCurrentUser(); // 重新查询最新用户数据
            // 2. 获取当前用户的权限 (通常从旧的 principal 中获取即可，除非权限也会随资料更新而改变)
            Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
            // 3. 创建一个新的 ForumUserDetails 对象，使用最新的用户实体数据
            ForumUserDetails updatedPrincipal = new ForumUserDetails(updatedUserEntity, authorities);
            // 4. 创建一个新的 Authentication Token
            //    这里的密码通常不需要是明文，可以使用旧 principal 的密码哈希，或者如果不需要密码在 principal 中可访问，可以设为 null
            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    updatedPrincipal, // 新的 UserDetails 对象
                    userDetails.getPassword(), // 使用旧的密码哈希 (或其他凭证)
                    authorities); // 权限

            // 5. 将新的 Authentication Token 设置到 SecurityContextHolder 中
            SecurityContextHolder.getContext().setAuthentication(newAuth);
            // **手动更新完成**
            

            
            redirectAttributes.addFlashAttribute("successMessage", "个人资料更新成功");
            return "redirect:/user/profile";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "头像上传失败：" + e.getMessage());
            return "redirect:/user/profile/edit";
        } catch (Exception e) {
//            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "个人资料更新失败：" + e.getMessage());
            return "redirect:/user/profile/edit";
        }
    }

    // /user/profile/password -> user/change-password.html
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