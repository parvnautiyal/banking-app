package org.bank.project.app.user.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.bank.project.app.account.entity.Account;
import org.bank.project.app.exception.BankingException;
import org.bank.project.app.security.JWTUtil;
import org.bank.project.app.security.PBKDF2Encoder;
import org.bank.project.app.user.entity.Address;
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

import java.util.*;
import java.util.stream.Collectors;

import static org.bank.project.app.util.Constants.*;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PBKDF2Encoder passwordEncoder;
    private final JWTUtil jwtUtil;
    private final ObjectMapper objectMapper;

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
        response.put(MESSAGE, "new token generated successfully");
        response.put("token", newToken);
        payload.put(RESPONSE, response);
        return payload;
    }

    @Override
    public Mono<Payload> changePassword(String path, String email, String password) {
        return userRepository.findByEmail(email).flatMap(user -> {
            user.setPassword(passwordEncoder.encode(password));
            return userRepository.save(user).flatMap(newUser -> {
                Payload payload = new Payload(path, HttpStatus.OK);
                payload.put(MESSAGE, String.format("Password updated for user with email %s", newUser.getEmail()));
                return Mono.just(payload);
            });
        });
    }

    @Override
    public Mono<Payload> getUserDetails(String username, String path) {
        return userRepository.findByUsername(username).flatMap(user -> {
            user.setPassword("****");
            Payload payload = new Payload(path, HttpStatus.OK);
            payload.put(RESPONSE, user);
            return Mono.just(payload);
        }).switchIfEmpty(Mono
                .error(new BankingException(HttpStatus.BAD_REQUEST, this.getErrorPayload(path, "invalid username"))));
    }

    @Override
    public Mono<Payload> updateUserDetails(Map<String, Object> updatedUser, String username, String path) {
        return userRepository.findByUsername(username).flatMap(user -> {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put(MESSAGE, "User updated");
            response.put("username", username);
            if (updatedUser.containsKey(PHONE_NUMBER)) {
                String phoneNumber = ((String) updatedUser.get(PHONE_NUMBER));
                user.setPhoneNumber(phoneNumber);
                response.put(PHONE_NUMBER, phoneNumber);
            }
            if (updatedUser.containsKey(ADDRESS)) {
                Address address = objectMapper.convertValue(updatedUser.get(ADDRESS), Address.class);
                user.setAddress(address);
                response.put(ADDRESS, address);
            }
            return userRepository.save(user).flatMap(newUser -> {
                Payload payload = new Payload(path, HttpStatus.OK);
                payload.put(RESPONSE, response);
                return Mono.just(payload);
            });
        }).switchIfEmpty(Mono.error(
                new BankingException(HttpStatus.BAD_REQUEST, this.getErrorPayload(path, "user does not exist"))));
    }

    @Override
    public Mono<Payload> openAccount(String path, Account account) {
        return userRepository.findByUsername(account.getUsername()).flatMap(user -> {
            Map<String, Account> accounts = new HashMap<>(user.getAccounts());
            if (accounts.containsKey(account.getAccountType().toString()))
                return Mono.error(new BankingException(HttpStatus.BAD_REQUEST,
                        this.getErrorPayload(path, "Account already exists")));
            accounts.put(account.getAccountType().toString(), account);
            user.setAccounts(accounts);
            return userRepository.save(user).flatMap(updatedUser -> {
                Payload payload = new Payload(path, HttpStatus.CREATED);
                UserResponseDTO userResponseDTO = UserResponseDTO.buildUserResponse(updatedUser.getId(),
                        updatedUser.getUsername(), updatedUser.getRoles(), updatedUser.getAccounts());
                payload.put(RESPONSE, userResponseDTO);
                return Mono.just(payload);
            });
        }).switchIfEmpty(Mono.error(
                new BankingException(HttpStatus.BAD_REQUEST, this.getErrorPayload(path, "Account already exists"))));
    }

    @Override
    public Mono<Payload> deleteUser(String path, String username) {
        return userRepository.findByUsername(username)
                .flatMap(user -> userRepository.deleteById(user.getId()).then(Mono.defer(() -> {
                    Payload payload = new Payload(path, HttpStatus.OK);
                    Map<String, String> res = new HashMap<>();
                    res.put(MESSAGE, String.format("user with username: %s deleted", username));
                    res.put("additionalInfo", "all accounts closed");
                    payload.put(RESPONSE, res);
                    return Mono.just(payload);
                }))).switchIfEmpty(Mono.error(
                        new BankingException(HttpStatus.BAD_REQUEST, this.getErrorPayload(path, USER_DOES_NOT_EXIST))));
    }

    @Override
    public Mono<Payload> deleteAccount(String path, String username, String accountNumber) {
        return userRepository.findByUsername(username).flatMap(user -> {
            Map<String, Account> accountMap = user.getAccounts().entrySet().stream()
                    .filter(stringAccountEntry -> !stringAccountEntry.getValue().getId().equals(accountNumber))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            user.setAccounts(accountMap);
            return userRepository.save(user).flatMap(updatedUser -> {
                Payload payload = new Payload(path, HttpStatus.OK);
                payload.put(MESSAGE, String.format("account number %s closed", accountNumber));
                return Mono.just(payload);
            });
        }).switchIfEmpty(Mono
                .error(new BankingException(HttpStatus.BAD_REQUEST, this.getErrorPayload(path, USER_DOES_NOT_EXIST))));
    }

    public Mono<List<String>> getAccountNumbers(String path, String username) {
        return userRepository.findByUsername(username).flatMap(user -> {
            List<String> accountNumbers = new ArrayList<>();
            user.getAccounts().forEach((s, account) -> accountNumbers.add(account.getId()));
            return Mono.just(accountNumbers);
        }).switchIfEmpty(Mono
                .error(new BankingException(HttpStatus.BAD_REQUEST, this.getErrorPayload(path, USER_DOES_NOT_EXIST))));
    }

    private Payload getErrorPayload(String path, String message) {
        Payload payload = new Payload(path, HttpStatus.BAD_REQUEST);
        payload.put(RESPONSE, message);
        return payload;
    }
}
