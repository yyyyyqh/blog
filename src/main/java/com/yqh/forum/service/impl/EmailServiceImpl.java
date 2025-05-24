package com.yqh.forum.service.impl;

import com.yqh.forum.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender; // 推荐使用 final
    private final String fromEmailAddress;       // 推荐使用 final
    private final String applicationName;
    private final String applicationLoginUrl; // 新增应用登录URL

    // 构造函数注入是推荐的方式
    @Autowired // 当有多个构造函数时，@Autowired 指明Spring应该使用哪一个。如果只有一个构造函数，此注解通常是可选的。
    public EmailServiceImpl(JavaMailSender javaMailSender,
                            @Value("${spring.mail.username}") String fromEmailAddress) { // <-- @Value 注解在这里
        this.javaMailSender = javaMailSender;
        this.fromEmailAddress = fromEmailAddress;
        this.applicationName = "论坛系统";
        this.applicationLoginUrl = "http://localhost:8080/post";
    }


    /**
     * 发送包含新（临时）密码的邮件给用户。
     *
     * @param to 收件人邮箱地址
     * @param userName 用户名，用于个性化邮件
     * @param newTemporaryPassword 后端生成的新临时密码
     * @throws MailException 如果邮件发送失败
     */
    public void sendTemporaryPasswordEmail(String to, String userName, String newTemporaryPassword) throws MailException {
        String subject = String.format("您的 [%s] 账户密码已重置", applicationName);

        String emailBody = String.format(
                "尊敬的 %s：\n\n" +
                        "您的 [%s] 账户（关联邮箱：%s）密码已由系统或管理员重置。\n\n" +
                        "您的新临时密码是： %s\n\n" +
                        "为了保障您的账户安全，请您务必：\n" +
                        "1. 立即访问我们的登录页面：%s\n" +
                        "2. 使用此临时密码登录。\n" +
                        "3. 登录后，请立即修改为您自己设置的、强度足够的唯一密码。\n\n" +
                        "如果您并未请求密码重置，或对此次操作有任何疑问（例如，怀疑是管理员误操作或账户存在风险），请不要使用此密码登录，并立即通过我们的官方渠道联系客服团队进行核实和处理。\n\n" +
                        "感谢您的合作。\n\n" +
                        "此致，\n" +
                        "%s 团队",
                userName,
                applicationName,
                to,
                newTemporaryPassword,
                applicationLoginUrl, // 提供登录页面的链接
                applicationName
        );

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(this.fromEmailAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(emailBody);

            this.javaMailSender.send(message);
            //logger.info("包含新临时密码的邮件已成功发送至：{}", to);
        } catch (MailException e) {
            //logger.error("发送包含新临时密码的邮件至 {} 失败：{}", to, e.getMessage(), e);
            throw e;
        }
    }


    //暂时未使用
    @Override
    public void sendPasswordResetEmail(String to, String userName, String resetLink) throws MailException {
        String subject = String.format("关于您在 [%s] 的密码重置请求", applicationName);

        // 通用的邮件正文，涵盖不同重置场景
        // 强调这是针对与该邮箱关联的账户的操作
        // resetLink 是用户设置新密码的入口
        String emailBody = String.format(
                "尊敬的 %s：\n\n" +
                        "我们收到或处理了一个针对您在 [%s]（邮箱地址：%s）账户的密码重置请求。\n\n" +
                        "请点击下面的链接来设置您的新密码。如果链接无法直接点击，请将其复制到浏览器地址栏中打开：\n" +
                        "%s\n\n" +
                        "为了您的账户安全，请注意：\n" +
                        "- 此链接具有时效性（例如，通常在 %s 小时内有效，请尽快操作）。\n" + // 最好能动态传入有效期或在此说明通用有效期
                        "- 如果您并未请求或授权此次密码重置，或者怀疑有未授权的账户活动，请不要点击此链接，并立即通过我们的官方渠道联系客服支持。\n\n" +
                        "感谢您的合作。\n\n" +
                        "此致，\n" +
                        "%s 团队",
                userName,
                applicationName,
                to, // 再次确认是发给哪个邮箱关联的账户
                resetLink,
                "1", // 示例：链接有效期1小时，可以根据实际情况调整或作为参数传入
                applicationName
        );

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(this.fromEmailAddress); // 发件人地址
            message.setTo(to);                      // 收件人地址
            message.setSubject(subject);            // 邮件主题
            message.setText(emailBody);             // 邮件正文

            this.javaMailSender.send(message); // 发送邮件
            //logger.info("密码重置邮件已成功发送至：{}", to);
        } catch (MailException e) {
            // 捕获邮件发送过程中可能出现的异常 (如邮箱地址无效、SMTP服务器问题等)
            //logger.error("发送密码重置邮件至 {} 失败：{}", to, e.getMessage(), e);
            throw e; // 将异常重新抛出，以便上层调用者可以感知并处理
        }
    }


}