package org.bank.project.app.exception;

import org.bank.project.app.util.Payload;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;
import reactor.core.publisher.Mono;

@ControllerAdvice
public class BankingExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BankingException.class)
    public Mono<ResponseEntity<Payload>> handleBankingException(BankingException exception) {
        return Mono.just(new ResponseEntity<>(exception.getPayload(), exception.getStatus()));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Payload>> handleBankingException(Exception exception, ServerHttpRequest request) {
        Payload payload = new Payload(request.getURI().getPath(), HttpStatus.INTERNAL_SERVER_ERROR);
        payload.put("message", exception.getMessage());
        return Mono.just(new ResponseEntity<>(payload, HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
