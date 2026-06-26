package br.com.gymflow.api.mapper;

import br.com.gymflow.api.domain.User;
import br.com.gymflow.api.dto.user.CreateUserRequest;
import br.com.gymflow.api.dto.user.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(CreateUserRequest request) {
        User user = new User();

        user.setName(request.name());
        user.setEmail(request.email());
        user.setRole(request.role());
        user.setActive(true);

        return user;
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getOrganization().getId(),
                user.getOrganization().getOrganizationName(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getActive(),
                user.getCreatedAt()
        );
    }
}