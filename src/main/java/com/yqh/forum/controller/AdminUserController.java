package com.yqh.forum.controller; // 请根据您的实际包结构调整

import com.yqh.forum.model.User;
import com.yqh.forum.service.EmailService;
import com.yqh.forum.service.UserService; // 假设您已经有了处理用户相关业务逻辑的 UserService
import com.yqh.forum.service.impl.EmailServiceImpl;
import lombok.RequiredArgsConstructor; // 如果您使用了 Lombok 的 @RequiredArgsConstructor
import org.springframework.security.access.prepost.PreAuthorize; // 导入 @PreAuthorize 注解
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller; // 导入 @Controller 注解
import org.springframework.ui.Model; // 导入 Model 类
import org.springframework.web.bind.annotation.GetMapping; // 导入 @GetMapping 注解
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping; // 导入 @RequestMapping 注解
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * 管理员用户的操作
 */
@Controller // 声明这是一个 Spring MVC 控制器
@RequestMapping("/admin/users") // 设置这个控制器的基础 URL 路径为 /admin/users
@RequiredArgsConstructor // Lombok 注解，为 final 字段自动生成构造函数进行依赖注入
@PreAuthorize("hasRole('ROLE_ADMIN')") // **重要：保护整个控制器，只有拥有 ROLE_ADMIN 角色的用户才能访问此控制器下的所有接口**
public class AdminUserController {

    private final UserService userService; // 注入 UserService，用于后续获取用户数据
    private final EmailService emailService;

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
    @GetMapping("/delete")
    //也可以单独对  @GetMapping("/delete") 进行权限控制
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    public String showUserDeletionList(Model model) {
        // 调用 UserService 获取所有用户列表
        List<User> users = userService.getAllUsers();


        // 将用户列表添加到 Model，以便在 Thymeleaf 模板中使用
        model.addAttribute("users", users);


        return "admin/users/delete-list"; // 返回对应的 Thymeleaf 模板文件路径
    }



    // --- 后续我们还将在这里添加处理实际用户删除、密码重置等 POST 请求的方法 ---

     /**
      * 处理用户删除请求
      * 处理 /admin/users/{id}/delete 的 POST 请求
      */
     @PostMapping("/{id}/delete")
     public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
         // 调用 UserService 执行用户删除逻辑
          userService.deleteUserById(id);
         // redirectAttributes.addFlashAttribute("successMessage", "用户删除成功！");
         // return "redirect:/admin/users/delete"; // 重定向回用户删除列表页

         return "redirect:/admin/users/delete";
     }



    /**
     * 显示用户密码重置管理列表页面
     * 处理 /admin/users/reset-password 的 GET 请求
     */
    @GetMapping("/reset-password") // 映射到 /admin/users/reset-password
    public String showPasswordResetList(Model model) {
        // 获取所有用户列表，添加到 Model
        List<User> users = userService.getAllUsers(); // 需要获取用户列表以显示在页面上

        // 将用户列表添加到 Model
        model.addAttribute("users", users);

        // 返回模板名称
        return "admin/users/reset-password-list";
    }


    // --- Methods for actual delete/reset actions ---

    /**
     * 处理用户删除请求 (待实现)
     * 处理 /admin/users/{id}/delete 的 POST 请求
     */
    // @PostMapping("/{id}/delete") // 取消注释并稍后实现删除逻辑
    // public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    //     // 调用 UserService 执行用户删除逻辑
    //     // userService.deleteUserById(id); // 您需要在 UserService 中实现此方法
    //     // redirectAttributes.addFlashAttribute("successMessage", "用户删除成功！");
    //     // return "redirect:/admin/users/delete"; // 重定向回用户删除列表页
    // }


    /**
     * 处理用户密码重置请求
     * 处理 /admin/users/{id}/reset-password 的 POST 请求
     */
    @PostMapping("/{id}/reset-password") // 映射到 /admin/users/{id}/reset-password
    public String resetUserPassword(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // 调用 UserService 执行密码重置逻辑
            // **重要：userService.resetUserPassword(id) 方法应该生成、编码、保存新密码，并负责将新密码通知用户（例如通过邮件）**
            // 如果 Service 方法返回了新密码，您可以选择在消息中显示它，但不建议在生产环境这样做，邮件通知更安全。
            // 如果 Service 方法内部处理了邮件发送且返回 void，下面的 success message 需要调整。

            String newPassword = userService.resetUserPassword(id); // 假设 Service 方法返回新生成的裸密码

            //将密码通过邮件发送给用户
            User to = userService.findById(id);
            String username = to.getUsername();
            String email = to.getEmail();

            emailService.sendTemporaryPasswordEmail(email, username, newPassword);

            // 添加成功消息（传递给thymeleaf视图，视图中通过${successMessage}可以访问）
            redirectAttributes.addFlashAttribute("successMessage", "用户密码已重置！请通过安全方式将新密码通知用户（例如发送邮件）");


        } catch (UsernameNotFoundException e) {
            // 处理用户不存在的情况
            redirectAttributes.addFlashAttribute("errorMessage", "密码重置失败：用户不存在！");
        } catch (Exception e) {
            // 处理其他潜在错误（例如密码生成/编码失败、数据库更新失败、邮件发送失败等）
            // 在实际应用中，您可能需要捕获更具体的 Service 层抛出的异常
            redirectAttributes.addFlashAttribute("errorMessage", "密码重置失败：" + e.getMessage());
        }

        // 重定向回密码重置列表页面
        return "redirect:/admin/users/reset-password";
    }


}