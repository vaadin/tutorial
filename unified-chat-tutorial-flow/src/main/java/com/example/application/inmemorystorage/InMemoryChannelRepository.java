package com.example.application.inmemorystorage;

import com.example.application.chat.Channel;
import com.example.application.chat.spi.ChannelRepository;
import com.example.application.chat.spi.MessageRepository;
import com.example.application.chat.spi.NewChannel;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
class InMemoryChannelRepository implements ChannelRepository {

    private final MessageRepository messageRepository;
    private final ConcurrentMap<String, Channel> channels = new ConcurrentHashMap<>();

    InMemoryChannelRepository(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public List<Channel> findAll() {
        return channels.values().stream()
                .sorted(Comparator.comparing(Channel::name))
                .map(this::addLatestMessageIfAvailable)
                .toList();
    }

    private Channel addLatestMessageIfAvailable(Channel channel) {
        return messageRepository.findLatest(channel.id(), 1).stream()
                .findFirst()
                .map(msg -> new Channel(channel.id(), channel.name(), msg))
                .orElse(channel);
    }

    @Override
    public Channel save(NewChannel newChannel) {
        var channel = new Channel(UUID.randomUUID().toString(), newChannel.name());
        channels.put(channel.id(), channel);
        return channel;
    }

    @Override
    public Optional<Channel> findById(String channelId) {
        return Optional.ofNullable(channels.get(channelId)).map(this::addLatestMessageIfAvailable);
    }

    @Override
    public boolean exists(String channelId) {
        return channels.containsKey(channelId);
    }
}
