package org.bank.project.app.user.controller;

import lombok.RequiredArgsConstructor;
import org.bank.project.app.security.PBKDF2Encoder;
import org.bank.project.app.user.entity.dto.AuthRequest;
import org.bank.project.app.user.entity.dto.UserDTO;
import org.bank.project.app.user.service.UserService;
import org.bank.project.app.util.Payload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/banking-app/api/rest/v1/auth")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthController {

    private final UserService userService;
    private final PBKDF2Encoder passwordEncoder;

    @PostMapping("register")
    public Mono<ResponseEntity<Payload>> registerUser(ServerHttpRequest request, @RequestBody UserDTO userDTO) {
        return userService.createUser(userDTO, request.getURI().getPath())
                .flatMap(payload -> Mono.just(ResponseEntity.created(request.getURI()).body(((Payload) payload))));
    }

    @PostMapping("login")
    public Mono<ResponseEntity<Payload>> login(@RequestBody AuthRequest ar, ServerHttpRequest request) {
        return userService.findUserByIdentifier(ar.getIdentifier())
                .filter(user -> passwordEncoder.encode(ar.getPassword()).equals(user.getPassword())).flatMap(user -> {
                    Payload payload = userService.loginUser(user, request.getURI().getPath());
                    return Mono.just(ResponseEntity.ok(payload));
                }).switchIfEmpty(Mono.just(new ResponseEntity<>(this.getErrorPayload(request.getURI().getPath()),
                        HttpStatus.UNAUTHORIZED)));
    }

    @GetMapping("refresh")
    public Mono<ResponseEntity<Payload>> refreshToken(@RequestHeader("REFRESH_TOKEN") String token,
            ServerHttpRequest request) {
        return Mono.just(new ResponseEntity<>(
                userService.generateTokenFromRefreshToken(token, request.getURI().getPath()), HttpStatus.OK));
    }

    private Payload getErrorPayload(String path) {
        Payload payload = new Payload(path, HttpStatus.UNAUTHORIZED);
        payload.put("message", "restricted access");
        return payload;
    }

}
