 package com.yqh.forum.service.impl; // 假设您的包名

import com.yqh.forum.repository.CommentRepository;
import com.yqh.forum.repository.PostRepository;
import com.yqh.forum.repository.UserRepository;
import com.yqh.forum.service.DashboardAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
public class DashboardAnalyticsServiceImpl implements DashboardAnalyticsService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public DashboardAnalyticsServiceImpl(UserRepository userRepository, PostRepository postRepository, CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public long getTotalUserCount() {
        return userRepository.count();
    }

    @Override
    public long getTotalPostCount() {
        return postRepository.count();
    }

    @Override
    public long getTotalCommentCount() {
        return commentRepository.count();
    }

    @Override
    public long getTodayActiveUserCount() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        Set<Long> activeUserIds = new HashSet<>();
        activeUserIds.addAll(postRepository.findDistinctAuthorIdsByCreatedAtBetween(startOfDay, endOfDay));
        activeUserIds.addAll(commentRepository.findDistinctAuthorIdsByCreatedAtBetween(startOfDay, endOfDay));

        return activeUserIds.size();
    }

    private Map<String, Long> getDailyCountsForLastNDays(int days, DailyCounter counter) {
        Map<String, Long> trendData = new LinkedHashMap<>(); //保持日期顺序
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd"); // 或者 "yyyy-MM-dd"

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX); // 包括当天的最后一秒

            long count = counter.count(startOfDay, endOfDay);
            trendData.put(date.format(formatter), count);
        }
        return trendData;
    }

    // 函数式接口用于传递不同的计数逻辑
    @FunctionalInterface
    interface DailyCounter {
        long count(LocalDateTime startOfDay, LocalDateTime endOfDay);
    }

    @Override
    public Map<String, Long> getUserRegistrationTrendLast7Days() {
        return getDailyCountsForLastNDays(7, (start, end) -> userRepository.countByCreatedAtBetween(start, end));
    }

    @Override
    public Map<String, Long> getPostCreationTrendLast7Days() {
        return getDailyCountsForLastNDays(7, (start, end) -> postRepository.countByCreatedAtBetween(start, end));
    }

    @Override
    public Map<String, Long> getCommentCreationTrendLast7Days() {
        return getDailyCountsForLastNDays(7, (start, end) -> commentRepository.countByCreatedAtBetween(start, end));
    }

    @Override
    public Map<String, Long> getDauTrendLast7Days() {
        return getDailyCountsForLastNDays(7, (startOfDay, endOfDay) -> {
            Set<Long> activeUserIds = new HashSet<>();
            activeUserIds.addAll(postRepository.findDistinctAuthorIdsByCreatedAtBetween(startOfDay, endOfDay));
            activeUserIds.addAll(commentRepository.findDistinctAuthorIdsByCreatedAtBetween(startOfDay, endOfDay));
            return activeUserIds.size();
        });
    }
}