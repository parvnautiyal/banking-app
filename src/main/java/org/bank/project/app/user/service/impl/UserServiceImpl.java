package org.bank.project.app.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.bank.project.app.exception.BankingException;
import org.bank.project.app.security.JWTUtil;
import org.bank.project.app.security.PBKDF2Encoder;
import org.bank.project.app.user.entity.User;
import org.bank.project.app.user.entity.dto.AuthResponse;
import org.bank.project.app.user.entity.dto.UserDTO;
import org.bank.project.app.user.entity.dto.UserResponseDTO;
import org.bank.project.app.user.repository.UserRepository;
import org.bank.project.app.user.service.UserService;
import org.bank.project.app.util.Payload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.bank.project.app.util.Constants.RESPONSE;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PBKDF2Encoder passwordEncoder;
    private final JWTUtil jwtUtil;

    public Mono<Object> createUser(UserDTO userDTO, String path) {
        return userRepository.findByUsername(userDTO.getUsername()).flatMap(user -> {
            Payload payload = new Payload(path, HttpStatus.BAD_REQUEST);
            payload.put(RESPONSE, "username already taken");
            return Mono.error(new BankingException(HttpStatus.BAD_REQUEST, payload));
        }).switchIfEmpty(userRepository.findByEmail(userDTO.getEmail()).flatMap(user -> {
            Payload payload = new Payload(path, HttpStatus.BAD_REQUEST);
            payload.put(RESPONSE, "email already taken");
            return Mono.error(new BankingException(HttpStatus.BAD_REQUEST, payload));
        }).switchIfEmpty(userRepository.save(User.buildUser(userDTO, passwordEncoder)).flatMap(user -> {
            Payload payload = new Payload(path, HttpStatus.CREATED);
            UserResponseDTO userResponseDTO = UserResponseDTO.buildUserResponse(user.getId(), user.getUsername(),
                    user.getRoles());
            payload.put(RESPONSE, userResponseDTO);
            return Mono.just(payload);
        })));
    }

    @Override
    public Mono<User> findUserByIdentifier(String identifier) {
        if (identifier.contains("@"))
            return userRepository.findByEmail(identifier);
        return userRepository.findByUsername(identifier);
    }

    @Override
    public Payload loginUser(User user, String path) {
        String token = jwtUtil.generateToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        String expirationDate = jwtUtil.getExpirationDateFromToken(token).toString();
        AuthResponse authResponse = new AuthResponse(token, refreshToken, expirationDate);
        Payload payload = new Payload(path, HttpStatus.OK);
        payload.put(RESPONSE, authResponse);
        return payload;
    }

    @Override
    public Payload generateTokenFromRefreshToken(String refreshToken, String path) {
        String newToken = jwtUtil.generateTokenFromRefreshToken(refreshToken);
        Payload payload = new Payload(path, HttpStatus.OK);
        Map<String, String> response = new LinkedHashMap<>();
        response.put("message", "new token generated successfully");
        response.put("token", newToken);
        payload.put(RESPONSE, response);
        return payload;
    }
}
