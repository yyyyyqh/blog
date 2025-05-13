package com.yqh.forum.controller; // 请根据您的实际包结构调整

import com.yqh.forum.model.User;
import com.yqh.forum.service.UserService; // 假设您已经有了处理用户相关业务逻辑的 UserService
import lombok.RequiredArgsConstructor; // 如果您使用了 Lombok 的 @RequiredArgsConstructor
import org.springframework.security.access.prepost.PreAuthorize; // 导入 @PreAuthorize 注解
import org.springframework.stereotype.Controller; // 导入 @Controller 注解
import org.springframework.ui.Model; // 导入 Model 类
import org.springframework.web.bind.annotation.GetMapping; // 导入 @GetMapping 注解
import org.springframework.web.bind.annotation.RequestMapping; // 导入 @RequestMapping 注解

import java.util.List;

@Controller // 声明这是一个 Spring MVC 控制器
@RequestMapping("/admin/users") // 设置这个控制器的基础 URL 路径为 /admin/users
@RequiredArgsConstructor // Lombok 注解，为 final 字段自动生成构造函数进行依赖注入
@PreAuthorize("hasRole('ROLE_ADMIN')") // **重要：保护整个控制器，只有拥有 ROLE_ADMIN 角色的用户才能访问此控制器下的所有接口**
public class AdminUserController {

    private final UserService userService; // 注入 UserService，用于后续获取用户数据

    /**
     * 显示用户管理仪表板页面
     * 处理 /admin/users 的 GET 请求
     */
    @GetMapping("/dashboard") // 映射到 /admin/users
    public String showDashboard(Model model) {
        // 在这里您可以选择向 Model 添加一些用于仪表板概览的数据，例如用户总数等
        // model.addAttribute("userCount", userService.getUserCount());
        return "admin/users/dashboard"; // 返回对应的 Thymeleaf 模板文件路径
    }

    // --- 后续我们将在这里添加处理用户删除列表、密码重置列表等方法的 Endpoint ---

    /**
     * 显示用户删除管理列表页面
     * 处理 /admin/users/delete 的 GET 请求
     */
    @GetMapping("/delete") // 映射到 /admin/users/delete
    public String showUserDeletionList(Model model) {
        System.out.println("--- Entering showUserDeletionList method ---"); // 进入方法日志

        // 调用 UserService 获取所有用户列表
        // **重要：请确保您的 UserService 中有 getAllUsers() 方法**
        List<User> users = userService.getAllUsers();

        // --- 添加日志打印：检查 userService.getAllUsers() 的返回值 ---
        System.out.println("Result from userService.getAllUsers(): " + (users == null ? "null" : "List with size " + users.size()));
        if (users != null) {
            // 如果返回的不是 null，可以打印一些列表内容（如果列表不长的话）
            if (!users.isEmpty() && users.size() <= 5) { // 只打印前5个用户，避免日志过长
                System.out.println("First few users from service: " + users);
            } else if (!users.isEmpty()) {
                System.out.println("userService.getAllUsers() returned a non-empty list (size > 5).");
            }
        }
        // --- 日志打印结束 ---


        // 将用户列表添加到 Model，以便在 Thymeleaf 模板中使用
        model.addAttribute("users", users);

        // --- 添加日志打印：检查 Model 是否成功添加了 "users" 属性 ---
        System.out.println("Added 'users' to Model.");
        System.out.println("Model contains attribute 'users': " + model.containsAttribute("users"));
        if (model.containsAttribute("users")) {
            Object modelUsers = model.getAttribute("users");
            System.out.println("'users' attribute in Model is of type: " + (modelUsers == null ? "null" : modelUsers.getClass().getName()));
            if (modelUsers instanceof List) {
                System.out.println("'users' attribute in Model list size: " + ((List) modelUsers).size());
            }
        } else {
            System.out.println("'users' attribute was NOT found in the Model after adding.");
        }
        System.out.println("--- Exiting showUserDeletionList method ---"); // 退出方法日志


        return "admin/users/delete-list"; // 返回对应的 Thymeleaf 模板文件路径
    }

    // /**
    //  * 显示用户密码重置管理列表页面
    //  * 处理 /admin/users/reset-password 的 GET 请求
    //  */
    // @GetMapping("/reset-password")
    // public String showPasswordResetList(Model model) {
    //     // 获取所有用户列表，添加到 Model
    //     // List<User> users = userService.getAllUsers();
    //     // model.addAttribute("users", users);
    //     return "admin/users/reset-password-list"; // 返回对应的 Thymeleaf 模板文件路径
    // }

    // --- 后续我们还将在这里添加处理实际用户删除、密码重置等 POST 请求的方法 ---

    // /**
    //  * 处理用户删除请求
    //  * 处理 /admin/users/{id}/delete 的 POST 请求
    //  */
    // @PostMapping("/{id}/delete")
    // public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    //     // 调用 UserService 执行用户删除逻辑
    //     // userService.deleteUserById(id);
    //     // redirectAttributes.addFlashAttribute("successMessage", "用户删除成功！");
    //     // return "redirect:/admin/users/delete"; // 重定向回用户删除列表页
    // }

    // /**
    //  * 处理用户密码重置请求
    //  * 处理 /admin/users/{id}/reset-password 的 POST 请求
    //  */
    // @PostMapping("/{id}/reset-password")
    // public String resetUserPassword(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    //     // 调用 UserService 执行密码重置逻辑（生成新密码，更新数据库，发送邮件等）
    //     // userService.resetUserPassword(id); // 您需要在 UserService 中实现此方法
    //     // redirectAttributes.addFlashAttribute("successMessage", "用户密码已重置！新密码已发送到用户邮箱。"); // 或其他通知方式
    //     // return "redirect:/admin/users/reset-password"; // 重定向回密码重置列表页
    // }

}