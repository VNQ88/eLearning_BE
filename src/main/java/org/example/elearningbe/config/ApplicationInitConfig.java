package org.example.elearningbe.config;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.elearningbe.role.RoleRepository;
import org.example.elearningbe.role.entities.Role;
import org.example.elearningbe.user.UserRepository;
import org.example.elearningbe.user.entities.User;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

@EnableAsync
@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;
    @Bean
    @ConditionalOnProperty(
            prefix = "spring",
            value = "datasource.driverClassName",
            havingValue = "com.postgresql.jdbc.Driver",
            matchIfMissing = true)
    ApplicationRunner applicationRunner(UserRepository userRepository) {
        log.info("Init application .....");
        return args -> {

            if ((roleRepository.findByName("ADMIN").isEmpty())) {
                Role role = Role.builder().name("ADMIN").build();
                roleRepository.save(role);
                log.info("Role 'ADMIN' has been created");
            }

            if ((roleRepository.findByName("STUDENT").isEmpty())) {
                Role role = Role.builder().name("STUDENT").build();
                roleRepository.save(role);
                log.info("Role 'STUDENT' has been created");
            }

            if (Boolean.FALSE.equals(userRepository.existsByEmail("admin@gmail.com"))) {
                HashSet<Role> roles = new HashSet<>();
                roleRepository.findByName("ADMIN").ifPresent(roles::add);
                User user = User.builder()
                        .email("admin@gmail.com")
                        .password("admin123")
                        .fullName("admin")
                        .enabled(true)
                        .roles(roles)
                        .build();
                userRepository.save(user);
                log.warn("admin user has been created with default email: admin@gmail.com password: admin123, please change it");
            }


        };
    }
}
