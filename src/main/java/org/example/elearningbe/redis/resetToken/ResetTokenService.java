package org.example.elearningbe.redis.resetToken;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResetTokenService {
    private final ResetTokenRepository resetTokenRepository;

    public void save(ResetToken resetToken) {
        resetTokenRepository.save(resetToken);
    }

    public ResetToken findById(String id) {
        return resetTokenRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid id " + id));
    }

    public void delete(String id) {
        ResetToken resetToken = findById(id);
        resetTokenRepository.delete(resetToken);
    }
}
