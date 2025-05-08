package com.yqh.forum.controller;

import com.yqh.forum.dto.UserDTO;
import com.yqh.forum.dto.UserRegistrationDTO;
import com.yqh.forum.model.User;
import com.yqh.forum.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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
                              RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.getCurrentUser();
            currentUser.setEmail(userDTO.getEmail());
            userService.updateUser(currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "个人信息更新成功");
            return "redirect:/user/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "更新失败：" + e.getMessage());
            return "redirect:/user/profile/edit";
        }
    }
} 