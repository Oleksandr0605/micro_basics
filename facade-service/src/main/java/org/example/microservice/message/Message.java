package org.example.microservice.message;

import java.util.UUID;

public class Message {
    private final UUID id;
    private final String text;

    public Message(UUID id, String text) {
        this.id = id;
        this.text = text;
    }

    public UUID getId() {
        return id;
    }

    public String getText() {
        return text;
    }
}
