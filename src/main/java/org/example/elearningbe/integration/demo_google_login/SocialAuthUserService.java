package org.example.elearningbe.integration.demo_google_login;

// package org.example.elearningbe.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.elearningbe.integration.demo_google_login.dto.GoogleUserInfo;
import org.example.elearningbe.role.RoleRepository;
import org.example.elearningbe.role.entities.Role;
import org.example.elearningbe.user.entities.User;
import org.example.elearningbe.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SocialAuthUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public User findOrCreateFromGoogle(GoogleUserInfo info) {
        return userRepository.findByEmail(info.getEmail())
                .orElseGet(() -> {
                    // role mặc định cho user mới, ví dụ "STUDENT"
                    Role defaultRole = roleRepository.findByName("STUDENT")
                            .orElseThrow(() -> new RuntimeException("Role STUDENT not found"));

                    User user = new User();
                    user.setEmail(info.getEmail());
                    user.setFullName(info.getName() != null ? info.getName() : info.getEmail());
                    user.setAvatar(info.getPicture());
                    user.setEnabled(true);

                    // đặt 1 password ngẫu nhiên (vì field không nullable + có @PrePersist encode) :contentReference[oaicite:7]{index=7}
                    String randomPassword = UUID.randomUUID().toString();
                    user.setPassword(passwordEncoder.encode(randomPassword));

                    user.setRoles(Set.of(defaultRole));
                    return userRepository.save(user);
                });
    }
}

