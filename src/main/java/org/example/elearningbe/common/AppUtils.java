package org.example.elearningbe.common;

import org.example.elearningbe.exception.InvalidDataException;
import org.example.elearningbe.lesson.entities.Lesson;
import org.example.elearningbe.user.entities.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class AppUtils {
    public static String getCurrentUserEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername(); // chính là email
        }
        return principal.toString();
    }

    public static void checkCourseOwner(Lesson lesson, String currentUser) {
        if (!lesson.getCourse().getOwner().getEmail().equals(currentUser)) {
            throw new InvalidDataException("You are not the owner of this course");
        }
    }

    public static void checkAdminOrCourseOwner(User currentUser, Lesson lesson) {
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().equalsIgnoreCase("ADMIN"));

        if (isAdmin) return; // ADMIN luôn pass

        // TEACHER nhưng không phải chủ khóa học thì throw
        if (!lesson.getCourse().getOwner().getEmail().equals(currentUser.getEmail())) {
            throw new InvalidDataException("You do not have permission for this action");
        }
    }
}

