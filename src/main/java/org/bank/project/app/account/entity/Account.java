package org.bank.project.app.account.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Document(collection = "account")
@JsonPropertyOrder({ "id", "userId", "username", "accountType", "balance" })
public class Account {
    @Id
    @JsonProperty("accountNumber")
    private String id;
    private String userId;
    private String username;
    private long balance;
    private AccountType accountType;
}
