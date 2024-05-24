package com.example.application.chat;

public class InvalidChannelException extends IllegalArgumentException {

    public InvalidChannelException() {
        super("The specified channel does not exist");
    }
}
