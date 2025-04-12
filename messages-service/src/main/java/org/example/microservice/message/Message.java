package org.example.microservice.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

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

    public Message(String value) {
        String[] parts = value.split("\\|", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid message format. Expected format: 'uuid|messageText'");
        }
        UUID id = UUID.fromString(parts[0]);
        String text = parts[1];
        this.id = id;
        this.text = text;
    }

    public String toString() {
        return id.toString() + "|" + text;
    }
}
