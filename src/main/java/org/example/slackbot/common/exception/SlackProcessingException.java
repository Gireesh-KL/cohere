package org.example.slackbot.common.exception;

public class SlackProcessingException extends RuntimeException {
    public SlackProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}


