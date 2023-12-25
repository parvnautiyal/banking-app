package org.bank.project.app.account.service;

import org.bank.project.app.account.entity.Account;
import org.bank.project.app.account.entity.AccountType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public interface AccountService {
    Mono<Account> openAccount(String username, String userId, long balance, AccountType accountType);

    Mono<Void> closeAccounts(List<String> accountNumbers);

    Mono<Void> closeAccount(String accountNumber);
}
