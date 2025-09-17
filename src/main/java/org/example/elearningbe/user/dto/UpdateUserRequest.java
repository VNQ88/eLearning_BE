package org.example.elearningbe.user.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@Data
public class UpdateUserRequest implements Serializable {
    private String email;
    private String fullName;
    private String password;
    private MultipartFile avatar;

}
