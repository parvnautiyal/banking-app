package org.bank.project.app.account.repository;

import org.bank.project.app.account.entity.Account;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface AccountRepository extends ReactiveMongoRepository<Account, String> {
    Mono<Void> deleteByIdIn(List<String> ids);
}
