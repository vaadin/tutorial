package com.example.application.chat;

import java.time.Instant;

public record Message(
        // TODO This should be a domain primitive, but it's currently not supported https://github.com/vaadin/hilla/issues/2055
        String messageId,
        String channelId,
        Long sequenceNumber,
        Instant timestamp,
        String author,
        String message) {
}
