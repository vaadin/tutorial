package com.example.application.chat;

import com.example.application.chat.spi.ChannelRepository;
import com.example.application.chat.spi.MessageRepository;
import com.example.application.chat.spi.NewChannel;
import com.example.application.chat.spi.NewMessage;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    private static final Duration BUFFER_DURATION = Duration.ofMillis(500);
    private final ChannelRepository channelRepository;
    private final MessageRepository messageRepository;
    private final Clock clock;
    private final Sinks.Many<Message> sink = Sinks.many().multicast().directBestEffort();

    public ChatService(ChannelRepository channelRepository, MessageRepository messageRepository, Clock clock) {
        this.channelRepository = channelRepository;
        this.messageRepository = messageRepository;
        this.clock = clock;
        generateTestData();
    }

    private void generateTestData() {
        String[] chatChannels = {
                "TechTalks Central",
                "Mindful Mornings",
                "Global Gourmet Guild",
                "Fitness Frontiers",
                "Bookworm Bungalow",
                "Creative Corner",
                "Eco Enthusiasts",
                "History Huddle",
                "Music Mavens",
                "Travel Trekkers",
                "Gamer's Grind",
                "Pet Parade",
                "Fashion Forward",
                "Science Sphere",
                "Artists' Alley",
                "Movie Maniacs",
                "Entrepreneur Exchange",
                "Health Hub",
                "DIY Den",
                "Language Labyrinth"
        };
        for (String channelName : chatChannels) {
            var channel = createChannel(channelName);
            log.info("Created channel: {} (http://localhost:8080/channel/{})", channel.name(), channel.id());
        }
    }

    public List<Channel> channels() {
        return channelRepository.findAll();
    }

    public Channel createChannel(String name) {
        return channelRepository.save(new NewChannel(name));
    }

    public Optional<Channel> channel(String channelId) {
        return channelRepository.findById(channelId);
    }

    public Flux<List<Message>> liveMessages(String channelId) throws InvalidChannelException {
        if (!channelRepository.exists(channelId)) {
            throw new InvalidChannelException();
        }
        return sink.asFlux().filter(m -> m.channelId().equals(channelId)).buffer(BUFFER_DURATION);
    }

    public List<Message> messageHistory(String channelId, int fetchMax, @Nullable String lastSeenMessageId) {
        return messageRepository.findLatest(channelId, fetchMax, lastSeenMessageId);
    }

    public void postMessage(String channelId, String message) throws InvalidChannelException {
        if (!channelRepository.exists(channelId)) {
            throw new InvalidChannelException();
        }
        var author = "John Doe";
        var msg = messageRepository.save(new NewMessage(channelId, clock.instant(), author, message));
        var result = sink.tryEmitNext(msg);
        if (result.isFailure()) {
            log.error("Error posting message to channel {}: {}", channelId, result);
        }
    }
}
