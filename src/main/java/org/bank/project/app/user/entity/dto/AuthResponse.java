package org.bank.project.app.user.entity.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AuthResponse {
    private String token;
    private String refreshToken;
    private String expirationDate;
}
