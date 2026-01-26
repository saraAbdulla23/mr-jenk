package com.user_service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.user_service.repository.UserRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AvatarEventListener {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AvatarEventListener(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @KafkaListener(
        topics = "avatar-updated-topic",
        groupId = "user-service"
    )
    public void onAvatarUpdated(String message) {
        try {
            Map<String, String> payload = objectMapper.readValue(message, Map.class);

            String userId = payload.get("userId");
            String avatarUrl = payload.get("avatarUrl");

            if (userId == null || avatarUrl == null) {
                System.err.println("❌ Invalid avatar event payload: " + payload);
                return;
            }

            userRepository.findById(userId).ifPresentOrElse(user -> {
                user.setAvatar(avatarUrl);
                userRepository.save(user);
                System.out.println("✅ Avatar updated for user " + userId);
            }, () -> {
                System.err.println("⚠️ User not found for avatar update: " + userId);
            });

        } catch (Exception e) {
            System.err.println("❌ Failed to process avatar event: " + e.getMessage());
        }
    }
}
