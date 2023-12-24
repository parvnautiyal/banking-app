package org.bank.project.app.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bank.project.app.util.Payload;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@AllArgsConstructor
public class BankingException extends RuntimeException {
    private final HttpStatus status;
    private final Payload payload;
}
