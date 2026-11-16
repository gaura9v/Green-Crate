package com.greencrate.controller;

import java.util.List;
import java.util.Map;

import com.greencrate.service.CommerceService;
import com.greencrate.service.FeedbackNotificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {
    private final CommerceService commerce;
    private final FeedbackNotificationService notifications;

    public FeedbackController(CommerceService commerce, FeedbackNotificationService notifications) {
        this.commerce = commerce;
        this.notifications = notifications;
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        return commerce.feedback();
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Map<String, Object> data) {
        Map<String, Object> feedback = commerce.createFeedback(data);
        notifications.send(feedback);
        return feedback;
    }
}
