package com.moneymatters.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User ensureUserExists(String clerkUserId, String email) {
        return userRepository.findById(clerkUserId)
            .orElseGet(() -> {
                User user = new User();
                user.setClerkUserId(clerkUserId);
                user.setEmail(email != null ? email : "");
                return userRepository.save(user);
            });
    }
}
