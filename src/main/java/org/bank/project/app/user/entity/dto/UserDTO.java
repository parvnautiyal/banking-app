package org.bank.project.app.user.entity.dto;

import lombok.*;
import org.bank.project.app.user.entity.Address;
import org.bank.project.app.user.entity.Role;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserDTO {
    private String name;
    private String username;
    private String email;
    private String password;
    private String phoneNumber;
    private String dateOfBirth;
    private Address address;
    private Set<Role> roles;
}
