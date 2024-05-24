package com.example.application.chat;

import com.example.application.chat.spi.ChannelRepository;
import com.example.application.chat.spi.NewChannel;
import com.example.application.security.Roles;
import jakarta.annotation.PostConstruct;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.test.context.support.WithMockUser;
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
    @DisplayName("All public methods require authentication")
    public void all_methods_require_authentication() {
        assertThatThrownBy(() -> chatService.channels()).isInstanceOf(AuthenticationException.class);
        assertThatThrownBy(() -> chatService.createChannel("won't work")).isInstanceOf(AuthenticationException.class);
        assertThatThrownBy(() -> chatService.channel("won't work")).isInstanceOf(AuthenticationException.class);
        assertThatThrownBy(() -> chatService.liveMessages("won't work")).isInstanceOf(AuthenticationException.class);
        assertThatThrownBy(() -> chatService.messageHistory("won't work", 1, null)).isInstanceOf(AuthenticationException.class);
        assertThatThrownBy(() -> chatService.postMessage("won't work", "will never get published")).isInstanceOf(AuthenticationException.class);
    }

    @Test
    @WithMockUser(roles = {})
    @DisplayName("All public methods require a role even if the user is authenticated")
    public void all_methods_require_a_role() {
        assertThatThrownBy(() -> chatService.channels()).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> chatService.createChannel("won't work")).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> chatService.channel("won't work")).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> chatService.liveMessages("won't work")).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> chatService.messageHistory("won't work", 1, null)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> chatService.postMessage("won't work", "will never get published")).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = Roles.USER)
    @DisplayName("Users can retrieve channels")
    public void users_can_retrieve_channels() {
        assertThat(chatService.channels()).isNotEmpty();
        assertThat(chatService.channel(knownChannelId)).isPresent();
    }

    @Test
    @WithMockUser(roles = {Roles.USER, Roles.ADMIN})
    @DisplayName("Admins can create channels")
    public void admins_can_create_channels() {
        var channel = chatService.createChannel("My channel");
        assertThat(chatService.channel(channel.id())).contains(channel);
        assertThat(chatService.channels()).contains(channel);
    }

    @Test
    @WithMockUser(roles = Roles.USER)
    @DisplayName("Users cannot create channels")
    public void users_cannot_create_channels() {
        assertThatThrownBy(() -> chatService.createChannel("won't work")).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(username = "joecool", roles = Roles.USER)
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
                            && message.author().equals("joecool")
                            && message.message().equals("Hello, world!");
                })
                .thenCancel()
                .verifyLater();
        chatService.postMessage(knownChannelId, "Hello, world!");
        verifier.verify();
    }

    @Test
    @WithMockUser(roles = Roles.USER)
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
    @WithMockUser(roles = Roles.USER)
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
    @WithMockUser(roles = Roles.USER)
    @DisplayName("Posting to a nonexistent channel throws an exception")
    public void posting_to_nonexistent_channel_throws_exception() {
        assertThatThrownBy(() -> chatService.postMessage("nonexistent", "will never get published")).isInstanceOf(InvalidChannelException.class);
    }

    @Test
    @WithMockUser(roles = Roles.USER)
    @DisplayName("Listening to a nonexistent channel throws an exception")
    public void listening_to_nonexistent_channel_throws_exception() {
        assertThatThrownBy(() -> chatService.liveMessages("nonexistent")).isInstanceOf(InvalidChannelException.class);
    }

    @Test
    @WithMockUser(roles = Roles.USER)
    @DisplayName("Fetching the message history of a nonexistent channel returns an empty list")
    public void fetching_message_history_of_nonexistent_channel_returns_empty_list() {
        Assertions.assertThat(chatService.messageHistory("nonexistent", 1, null)).isEmpty();
    }
}
