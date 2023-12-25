package org.bank.project.app.account.controller;

import lombok.RequiredArgsConstructor;
import org.bank.project.app.account.entity.Account;
import org.bank.project.app.account.entity.AccountType;
import org.bank.project.app.account.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/banking-app/api/rest/v1/account")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AccountController {

    private final AccountService accountService;

    @PostMapping("open-account")
    @PreAuthorize("hasRole('USER')")
    public Mono<ResponseEntity<Account>> openAccount(ServerHttpRequest request, @RequestBody Map<String, Object> req) {
        return accountService.openAccount(((String) req.get("username")), ((String) req.get("userId")),
                ((Integer) req.get("balance")).longValue(), AccountType.valueOf(((String) req.get("accountType"))))
                .flatMap(account -> Mono.just(ResponseEntity.created(request.getURI()).body(account)));
    }

    @PostMapping("close-accounts")
    @PreAuthorize("hasRole('USER')")
    public Mono<ResponseEntity<Void>> closeAccounts(@RequestBody List<String> accountNumbers) {
        return accountService.closeAccounts(accountNumbers)
                .then(Mono.defer(() -> Mono.just(new ResponseEntity<>(null, HttpStatus.NO_CONTENT))));
    }

    @PostMapping("close-account/{accountNumber}")
    @PreAuthorize("hasRole('USER')")
    public Mono<ResponseEntity<Void>> closeAccount(@PathVariable("accountNumber") String accountNumber) {
        return accountService.closeAccount(accountNumber)
                .then(Mono.defer(() -> Mono.just(new ResponseEntity<>(null, HttpStatus.NO_CONTENT))));
    }

}
