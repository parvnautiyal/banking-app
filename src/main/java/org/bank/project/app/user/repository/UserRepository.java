package org.bank.project.app.user.repository;

import org.bank.project.app.user.entity.User;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveMongoRepository<User, String> {

    Mono<User> findByUsername(String username);

    Mono<User> findByEmail(String email);

    @Query(value = "{email:?0}", fields = "{username: 1,_id: 0}")
    Mono<String> findUsernameByEmail(String email);

}
