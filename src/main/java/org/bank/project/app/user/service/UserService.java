package org.bank.project.app.user.service;

import org.bank.project.app.user.entity.User;
import org.bank.project.app.user.entity.dto.UserDTO;
import org.bank.project.app.util.Payload;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public interface UserService {

    Mono<Object> createUser(UserDTO userDTO, String path);

    Mono<User> findUserByIdentifier(String identifier);

    Payload loginUser(User user, String path);

    Payload generateTokenFromRefreshToken(String refreshToken, String path);

}
