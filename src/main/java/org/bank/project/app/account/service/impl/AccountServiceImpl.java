package org.bank.project.app.account.service.impl;

import lombok.RequiredArgsConstructor;
import org.bank.project.app.account.entity.Account;
import org.bank.project.app.account.entity.AccountType;
import org.bank.project.app.account.repository.AccountRepository;
import org.bank.project.app.account.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Override
    public Mono<Account> openAccount(String username, String userId, long balance, AccountType accountType) {
        return accountRepository.save(new Account(null, userId, username, balance, accountType));
    }

    @Override
    public Mono<Void> closeAccounts(List<String> accountNumbers) {
        return accountRepository.deleteByIdIn(accountNumbers);
    }

    @Override
    public Mono<Void> closeAccount(String accountNumber) {
        return accountRepository.deleteById(accountNumber);
    }
}
