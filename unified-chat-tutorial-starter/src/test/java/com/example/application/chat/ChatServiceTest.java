package com.example.application.chat;

import com.example.application.chat.spi.ChannelRepository;
import com.example.application.chat.spi.NewChannel;
import jakarta.annotation.PostConstruct;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("ReactiveStreamsUnusedPublisher")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class ChatServiceTest {

    private String knownChannelId;

    @Autowired
    ChatService chatService;

    @Autowired
    ChannelRepository channelRepository;

    @PostConstruct
    public void setUpAll() {
        knownChannelId = channelRepository.save(new NewChannel("General")).id();
    }

    @Test
    @DisplayName("Users can retrieve channels")
    public void users_can_retrieve_channels() {
        assertThat(chatService.channels()).isNotEmpty();
        assertThat(chatService.channel(knownChannelId)).isPresent();
    }

    @Test
    @DisplayName("Admins can create channels")
    public void admins_can_create_channels() {
        var channel = chatService.createChannel("My channel");
        assertThat(chatService.channel(channel.id())).contains(channel);
        assertThat(chatService.channels()).contains(channel);
    }

    @Test
    @DisplayName("Users can post and receive messages")
    public void users_can_post_and_receive_messages() {
        var liveMessages = chatService.liveMessages(knownChannelId);
        var verifier = StepVerifier
                .create(liveMessages)
                .expectNextMatches(messages -> {
                    if (messages.isEmpty()) {
                        return false;
                    }
                    var message = messages.getFirst();
                    return message.channelId().equals(knownChannelId)
                            && message.author().equals("John Doe")
                            && message.message().equals("Hello, world!");
                })
                .thenCancel()
                .verifyLater();
        chatService.postMessage(knownChannelId, "Hello, world!");
        verifier.verify();
    }

    @Test
    @DisplayName("Simultaneously posted messages are grouped together to save bandwidth")
    public void simultaneously_posted_messages_are_grouped_together_to_save_bandwidth() {
        var liveMessages = chatService.liveMessages(knownChannelId);
        var verifier = StepVerifier
                .create(liveMessages)
                .expectNextMatches(messages -> messages.size() == 2)
                .thenCancel()
                .verifyLater();
        chatService.postMessage(knownChannelId, "message1");
        chatService.postMessage(knownChannelId, "message2");
        verifier.verify();
    }

    @Test
    @DisplayName("Users can fetch message history")
    public void users_can_fetch_message_history() {
        chatService.postMessage(knownChannelId, "message1");
        chatService.postMessage(knownChannelId, "message2");
        chatService.postMessage(knownChannelId, "message3");
        chatService.postMessage(knownChannelId, "message4");
        chatService.postMessage(knownChannelId, "message5");
        chatService.postMessage(knownChannelId, "message6");
        Assertions.assertThat(chatService.messageHistory(knownChannelId, 5, null)).satisfies(messages -> {
            Assertions.assertThat(messages).hasSize(5);
            assertThat(messages.getFirst().message()).isEqualTo("message2");
            assertThat(messages.getLast().message()).isEqualTo("message6");
        });
    }

    @Test
    @DisplayName("Posting to a nonexistent channel throws an exception")
    public void posting_to_nonexistent_channel_throws_exception() {
        assertThatThrownBy(() -> chatService.postMessage("nonexistent", "will never get published")).isInstanceOf(InvalidChannelException.class);
    }

    @Test
    @DisplayName("Listening to a nonexistent channel throws an exception")
    public void listening_to_nonexistent_channel_throws_exception() {
        assertThatThrownBy(() -> chatService.liveMessages("nonexistent")).isInstanceOf(InvalidChannelException.class);
    }

    @Test
    @DisplayName("Fetching the message history of a nonexistent channel returns an empty list")
    public void fetching_message_history_of_nonexistent_channel_returns_empty_list() {
        Assertions.assertThat(chatService.messageHistory("nonexistent", 1, null)).isEmpty();
    }
}
