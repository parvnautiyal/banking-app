package org.bank.project.app.user.service;

import org.bank.project.app.account.entity.Account;
import org.bank.project.app.user.entity.User;
import org.bank.project.app.user.entity.dto.UserDTO;
import org.bank.project.app.util.Payload;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public interface UserService {

    Mono<Object> createUser(UserDTO userDTO, String path);

    Mono<User> findUserByIdentifier(String identifier);

    Payload loginUser(User user, String path);

    Payload generateTokenFromRefreshToken(String refreshToken, String path);

    Mono<Payload> getUserDetails(String username, String path);

    Mono<Payload> changePassword(String path, String email, String password);

    Mono<Payload> updateUserDetails(Map<String, Object> updatedUser, String username, String path);

    Mono<Payload> openAccount(String path, Account account);

    Mono<List<String>> getAccountNumbers(String path, String username);

    Mono<Payload> deleteUser(String path, String username);

    Mono<Payload> deleteAccount(String path, String username, String accountNumber);
}
