package org.bank.project.app.user.controller;

import lombok.RequiredArgsConstructor;
import org.bank.project.app.account.entity.Account;
import org.bank.project.app.user.service.UserService;
import org.bank.project.app.util.Payload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/banking-app/api/rest/v1/user")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserController {

    private final UserService userService;
    private final WebClient webClient;

    @GetMapping("details/{username}")
    @PreAuthorize("hasRole('USER')")
    public Mono<ResponseEntity<Payload>> getUserDetails(@PathVariable("username") String username,
            ServerHttpRequest request) {
        return userService.getUserDetails(username, request.getURI().getPath())
                .flatMap(payload -> Mono.just(ResponseEntity.ok(payload)));
    }

    @PatchMapping("edit/{username}")
    @PreAuthorize("hasRole('USER')")
    public Mono<ResponseEntity<Payload>> updateUserDetails(@PathVariable("username") String username,
            @RequestBody Map<String, Object> updatedUser, ServerHttpRequest request) {
        return userService.updateUserDetails(updatedUser, username, request.getURI().getPath())
                .flatMap(payload -> Mono.just(ResponseEntity.ok(payload)));
    }

    @PostMapping("open-account")
    @PreAuthorize("hasRole('USER')")
    public Mono<ResponseEntity<Payload>> openAccount(ServerHttpRequest request, @RequestBody Map<String, Object> req) {
        return webClient.post().uri("account/open-account")
                .header(HttpHeaders.AUTHORIZATION,
                        Objects.requireNonNull(request.getHeaders().get(HttpHeaders.AUTHORIZATION)).getFirst())
                .body(Mono.just(req), Map.class).retrieve().bodyToMono(Account.class)
                .flatMap(account -> userService.openAccount(request.getURI().getPath(), account)
                        .flatMap(payload -> Mono.just(ResponseEntity.ok(payload))));
    }

    @DeleteMapping("delete-user/{username}")
    @PreAuthorize("hasRole('USER')")
    public Mono<ResponseEntity<Payload>> deleteUser(ServerHttpRequest request,
            @PathVariable("username") String username) {
        String path = request.getURI().getPath();
        return webClient.post().uri("account/close-accounts")
                .header(HttpHeaders.AUTHORIZATION,
                        Objects.requireNonNull(request.getHeaders().get(HttpHeaders.AUTHORIZATION)).getFirst())
                .body(userService.getAccountNumbers(path, username), List.class).retrieve().toBodilessEntity()
                .flatMap(voidResponseEntity -> userService.deleteUser(path, username))
                .flatMap(payload -> Mono.just(ResponseEntity.ok(payload)));
    }

    @DeleteMapping("close-account/{username}/{accountNumber}")
    @PreAuthorize("hasRole('USER')")
    public Mono<ResponseEntity<Payload>> closeAccount(ServerHttpRequest request,
            @PathVariable("username") String username, @PathVariable("accountNumber") String accountNumber) {
        return webClient.post().uri("account/close-account/{accountNumber}", accountNumber)
                .header(HttpHeaders.AUTHORIZATION,
                        Objects.requireNonNull(request.getHeaders().get(HttpHeaders.AUTHORIZATION)).getFirst())
                .retrieve().toBodilessEntity()
                .flatMap(voidResponseEntity -> userService.deleteUser(request.getURI().getPath(), username)
                        .flatMap(payload -> Mono.just(ResponseEntity.ok(payload))));
    }
}
