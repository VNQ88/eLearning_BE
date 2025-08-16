package org.example.elearningbe.mapper;

import org.example.elearningbe.role.entities.Role;
import org.example.elearningbe.user.dto.UserResponse;
import org.example.elearningbe.user.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToRoleNames")
    UserResponse toUserResponse(User user);

    @Named("rolesToRoleNames")
    static Set<String> rolesToRoleNames(Set<Role> roles) {
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}
