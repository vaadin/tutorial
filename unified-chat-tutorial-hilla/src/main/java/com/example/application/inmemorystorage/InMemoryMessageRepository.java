package com.example.application.inmemorystorage;

import com.example.application.chat.Message;
import com.example.application.chat.spi.MessageRepository;
import com.example.application.chat.spi.NewMessage;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
class InMemoryMessageRepository implements MessageRepository {

    private final ConcurrentMap<String, MessageArchive> messageArchives = new ConcurrentHashMap<>();

    @Override
    public List<Message> findLatest(String channelId, int fetchMax, @Nullable String lastSeenMessageId) {
        if (fetchMax < 1) {
            throw new IllegalArgumentException("fetchMax must be at least 1");
        }
        return Optional.ofNullable(messageArchives.get(channelId))
                .map(archive -> archive.findLatest(fetchMax, lastSeenMessageId))
                .orElse(Collections.emptyList());
    }

    @Override
    public Message save(NewMessage newMessage) {
        return messageArchives.computeIfAbsent(newMessage.channelId(), MessageArchive::new).save(newMessage);
    }

    private static class MessageArchive {
        private final AtomicLong sequenceNumber = new AtomicLong(1);
        private final List<Message> messages = new ArrayList<>();
        private final ReadWriteLock lock = new ReentrantReadWriteLock();
        private final String channelId;

        private MessageArchive(String channelId) {
            this.channelId = channelId;
        }

        public List<Message> findLatest(int fetchMax, @Nullable String lastSeenMessageId) {
            lock.readLock().lock();
            try {
                var indexOfLastSeenMessage = lastSeenMessageId == null ? -1 : indexOfMessage(lastSeenMessageId);
                if (messages.size() - fetchMax > indexOfLastSeenMessage) {
                    return List.copyOf(messages.subList(messages.size() - fetchMax, messages.size()));
                } else {
                    return List.copyOf(messages.subList(indexOfLastSeenMessage + 1, messages.size()));
                }
            } finally {
                lock.readLock().unlock();
            }
        }

        private int indexOfMessage(String messageId) {
            for (int i = messages.size() - 1; i >= 0; i--) {
                if (messages.get(i).messageId().equals(messageId)) {
                    return i;
                }
            }
            return -1;
        }

        public Message save(NewMessage newMessage) {
            lock.writeLock().lock();
            try {
                var message = new Message(UUID.randomUUID().toString(), channelId, sequenceNumber.getAndIncrement(),
                        newMessage.timestamp(), newMessage.author(), newMessage.message());
                messages.add(message);
                return message;
            } finally {
                lock.writeLock().unlock();
            }
        }
    }
}
