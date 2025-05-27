package com.yqh.forum.controller; // 请根据您的实际包结构调整

import com.yqh.forum.model.User;
import com.yqh.forum.service.EmailService;
import com.yqh.forum.service.UserService; // 假设您已经有了处理用户相关业务逻辑的 UserService
// import com.yqh.forum.service.impl.EmailServiceImpl; // Assuming EmailService is an interface
import lombok.RequiredArgsConstructor; // 如果您使用了 Lombok 的 @RequiredArgsConstructor
import org.springframework.security.access.prepost.PreAuthorize; // 导入 @PreAuthorize 注解
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller; // 导入 @Controller 注解
import org.springframework.ui.Model; // 导入 Model 类
import org.springframework.web.bind.annotation.GetMapping; // 导入 @GetMapping 注解
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping; // 导入 @RequestMapping 注解
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

/**
 * 管理员用户的操作
 */
@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor // Lombok 注解，为 final 字段自动生成构造函数进行依赖注入
@PreAuthorize("hasRole('ROLE_ADMIN')") //只有拥有 ROLE_ADMIN 角色的用户才能访问此控制器下的所有接口，也可以只对单独的接口进行权限控制
public class AdminUserController {

    private final UserService userService; // 注入 UserService，用于后续获取用户数据
    private final EmailService emailService;

    /**
     * 显示统一的用户管理仪表板页面
     * 可以通过 'view' 参数切换显示内容 (charts, delete_list, reset_password_list)
     * 处理 /admin/users/dashboard 的 GET 请求
     */
    @GetMapping("/dashboard")
    public String showDashboard(@RequestParam(name = "view", defaultValue = "charts") String view, Model model) {
        model.addAttribute("currentView", view);
        // Add any data needed for stat cards universally here, e.g.:
        // model.addAttribute("totalUserCount", userService.getUserCount());
        // model.addAttribute("totalPostCount", postService.getPostCount()); // Assuming you have postService

        // 使用占位数据直到服务层实现:
        model.addAttribute("totalUserCount", userService.getAllUsers().size()); // 简单的示例
        model.addAttribute("totalPostCount", 5678); // 替换为 postService.getTotalPostCount()
        model.addAttribute("totalCommentCount", 12345); // 替换为 commentService.getTotalCommentCount()
        model.addAttribute("todayActiveUserCount", 150); // 替换为 userService.getTodayActiveUserCount()

        switch (view) {
            case "delete_list":
                List<User> usersForDelete = userService.getAllUsers();
                model.addAttribute("users", usersForDelete);
                model.addAttribute("title", "用户删除");
                break;
            case "reset_password_list":
                List<User> usersForReset = userService.getAllUsers();
                model.addAttribute("users", usersForReset);
                model.addAttribute("title", "密码重置");
                break;
            case "charts":
            default:
                // Add any data specific to charts if they are dynamic
                // e.g., model.addAttribute("userRegistrationData", userService.getWeeklyUserRegistration());
                model.addAttribute("title", "后台管理面板 - 数据可视化");
                model.addAttribute("currentView", "charts"); // Ensure default is explicitly set

                // 使用占位图表数据：
                List<String> last7DaysLabels = Arrays.asList("D-6", "D-5", "D-4", "D-3", "D-2", "昨天", "今天");
                model.addAttribute("userRegistrationLabels", last7DaysLabels);
                model.addAttribute("userRegistrationData", Arrays.asList(10, 15, 8, 12, 17, 20, 222));

                model.addAttribute("postCreationLabels", last7DaysLabels);
                model.addAttribute("postCreationData", Arrays.asList(25, 30, 22, 35, 40, 33, 45));

                model.addAttribute("commentCreationLabels", last7DaysLabels);
                model.addAttribute("commentCreationData", Arrays.asList(50, 55, 60, 45, 70, 80, 75));

                model.addAttribute("dauTrendLabels", last7DaysLabels);
                model.addAttribute("dauTrendData", Arrays.asList(100, 110, 105, 120, 130, 125, 140));
                break;
        }
        return "admin/users/admin_dashboard"; // Returns the main enhanced dashboard page
    }

    /**
     * 处理用户删除请求
     * 处理 /admin/users/{id}/delete 的 POST 请求
     */
    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUserById(id);
            redirectAttributes.addFlashAttribute("successMessage", "用户删除成功！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "用户删除失败：" + e.getMessage());
        }
        return "redirect:/admin/users/dashboard?view=delete_list"; // 重定向回用户删除列表视图
    }

    /**
     * 处理用户密码重置请求
     * 处理 /admin/users/{id}/reset-password 的 POST 请求
     */
    @PostMapping("/{id}/reset-password")
    public String resetUserPassword(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findById(id);
            if (user == null) {
                throw new UsernameNotFoundException("User not found with id: " + id);
            }
            String newPassword = userService.resetUserPassword(id); // Assumes this method handles saving the new password

            emailService.sendTemporaryPasswordEmail(user.getEmail(), user.getUsername(), newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "用户 " + user.getUsername() + " 的密码已重置并已通过邮件发送！");
        } catch (UsernameNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "密码重置失败：用户不存在！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "密码重置失败：" + e.getMessage());
            // Log the exception e
        }
        return "redirect:/admin/users/dashboard?view=reset_password_list"; // 重定向回密码重置列表视图
    }
}