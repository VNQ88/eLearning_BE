package org.example.elearningbe.user;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.example.elearningbe.common.PageResponse;
import org.example.elearningbe.exception.ResourceNotFoundException;
import org.example.elearningbe.mapper.UserMapper;
import org.example.elearningbe.user.dto.UserResponse;
import org.example.elearningbe.user.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public PageResponse<?> getAllUsers(int pageNo, @Min(10) int pageSize) {
        Page<User> userPage = userRepository.findAll(PageRequest.of(pageNo, pageSize));
        List<UserResponse> userResponses = userPage.getContent().stream()
                .map(userMapper::toUserResponse)
                .toList();
        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(userPage.getTotalPages())
                .items(userResponses)
                .build();
    }

    public UserResponse getUser(@Min(1) long userId) {
        User user = getUserById(userId);
        return userMapper.toUserResponse(user);
    }

    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return userMapper.toUserResponse(user);
    }

    private User getUserById(long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException(
                "User not found with id: " + userId));
    }

    public UserResponse getCurrentUser() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userRepository.findByEmail(userEmail);
        if (user.isEmpty()) {
            throw new ResourceNotFoundException("Current user not found");
        }
        return userMapper.toUserResponse(user.get());
    }
}
