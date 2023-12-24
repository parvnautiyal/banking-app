package org.bank.project.app.user.entity.dto;

import lombok.*;
import org.bank.project.app.user.entity.Role;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserResponseDTO {
    private String message;
    private UserResponse userResponse;

    public static UserResponseDTO buildUserResponse(String id, String username, Set<Role> roles) {
        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setMessage(String.format("User with id <%s> created", id));
        UserResponse uRes = new UserResponse();
        uRes.setId(id);
        uRes.setUsername(username);
        uRes.setRoles(roles);
        userResponseDTO.setUserResponse(uRes);
        return userResponseDTO;
    }
}

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
class UserResponse {
    private String id;
    private String username;
    private Set<Role> roles;
}
