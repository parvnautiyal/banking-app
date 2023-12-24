package org.bank.project.app.user.entity;

import lombok.*;
import org.bank.project.app.security.PBKDF2Encoder;
import org.bank.project.app.user.entity.dto.UserDTO;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@RequiredArgsConstructor
@Document(collection = "user")
public class User implements UserDetails {
    @Id
    private String id;
    private String name;
    @NonNull
    private String username;
    private String email;
    @NonNull
    private String password;
    private String phoneNumber;
    private String dateOfBirth;
    private Address address;
    private boolean enabled;
    private Set<Role> roles;
    @CreatedDate
    private LocalDateTime createdDate;
    @LastModifiedDate
    private LocalDateTime modifiedDate;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        this.roles.forEach(role -> authorities.add(new SimpleGrantedAuthority(role.name())));
        return authorities;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    public static User buildUser(UserDTO userDTO, PBKDF2Encoder passwordEncoder) {
        return new User(null, userDTO.getName(), userDTO.getUsername(), userDTO.getEmail(),
                passwordEncoder.encode(userDTO.getPassword()), userDTO.getPhoneNumber(), userDTO.getDateOfBirth(),
                userDTO.getAddress(), true, userDTO.getRoles(), null, null);
    }
}
