package com.yqh.forum.service;

import java.util.Map;

/**
 * 获取仪表盘的数据
 */
public interface DashboardAnalyticsService {
    long getTotalUserCount();
    long getTotalPostCount();
    long getTotalCommentCount();
    long getTodayActiveUserCount(); // 定义“今日活跃”

    Map<String, Long> getUserRegistrationTrendLast7Days(); // 返回 <日期字符串, 数量>
    Map<String, Long> getPostCreationTrendLast7Days();
    Map<String, Long> getCommentCreationTrendLast7Days();
    Map<String, Long> getDauTrendLast7Days(); // 定义“每日活跃用户”
}
