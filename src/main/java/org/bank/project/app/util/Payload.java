package org.bank.project.app.util;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;

public class Payload extends LinkedHashMap<String, Object> {

    public Payload(String path, HttpStatus status) {
        this.put("timestamp", LocalDateTime.now().toString());
        this.put("status", String.format("%s %s", status.value(), status.getReasonPhrase()));
        this.put("path", path);
    }
}
