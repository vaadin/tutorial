package com.example.application.inmemorystorage;

import com.example.application.chat.spi.MessageRepository;
import com.example.application.chat.spi.NewMessage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class InMemoryMessageRepositoryTest {

    private static final String CHANNEL1 = "channel1";
    private static final String CHANNEL2 = "channel2";
    private static final Instant TIMESTAMP1 = Instant.ofEpochMilli(1707380158462L);
    private static final Instant TIMESTAMP2 = TIMESTAMP1.plusSeconds(10);
    private static final Instant TIMESTAMP3 = TIMESTAMP2.plusSeconds(10);
    private MessageRepository repo;

    @BeforeEach
    void setup() {
        repo = new InMemoryMessageRepository();
    }

    @Test
    void repository_is_empty_at_first() {
        Assertions.assertThat(repo.findLatest(CHANNEL1, 10)).isEmpty();
        Assertions.assertThat(repo.findLatest(CHANNEL2, 10)).isEmpty();
    }

    @Test
    void repository_can_save_and_retrieve_messages() {
        var message1 = repo.save(new NewMessage(CHANNEL1, TIMESTAMP1, "user1", "message1"));
        var message2 = repo.save(new NewMessage(CHANNEL1, TIMESTAMP2, "user2", "message2"));
        var message3 = repo.save(new NewMessage(CHANNEL2, TIMESTAMP3, "user3", "message3"));

        assertThat(message1.sequenceNumber()).isEqualTo(1L);
        assertThat(message2.sequenceNumber()).isEqualTo(2L);
        assertThat(message3.sequenceNumber()).isEqualTo(1L);

        Assertions.assertThat(repo.findLatest(CHANNEL1, 10)).containsExactly(message1, message2);
        Assertions.assertThat(repo.findLatest(CHANNEL2, 10)).containsExactly(message3);
    }

    @Test
    void repository_can_retrieve_messages_after_a_certain_message() {
        var message1 = repo.save(new NewMessage(CHANNEL1, TIMESTAMP1, "user1", "message1"));
        var message2 = repo.save(new NewMessage(CHANNEL1, TIMESTAMP2, "user2", "message2"));
        var message3 = repo.save(new NewMessage(CHANNEL1, TIMESTAMP3, "user3", "message3"));

        Assertions.assertThat(repo.findLatest(CHANNEL1, 10, message1.messageId())).containsExactly(message2, message3);
    }

    @Test
    void repository_can_retrieve_limited_number_of_messages() {
        var message1 = repo.save(new NewMessage(CHANNEL1, TIMESTAMP1, "user1", "message1"));
        var message2 = repo.save(new NewMessage(CHANNEL1, TIMESTAMP2, "user2", "message2"));
        var message3 = repo.save(new NewMessage(CHANNEL1, TIMESTAMP3, "user3", "message3"));

        Assertions.assertThat(repo.findLatest(CHANNEL1, 2)).containsExactly(message2, message3);
    }

    @Test
    void repository_can_retrieve_limited_number_of_messages_after_a_certain_message() {
        var message1 = repo.save(new NewMessage(CHANNEL1, TIMESTAMP1, "user1", "message1"));
        var message2 = repo.save(new NewMessage(CHANNEL1, TIMESTAMP2, "user2", "message2"));
        var message3 = repo.save(new NewMessage(CHANNEL1, TIMESTAMP3, "user3", "message3"));

        Assertions.assertThat(repo.findLatest(CHANNEL1, 1, message1.messageId())).containsExactly(message3);
    }

    @Test
    void fetch_max_must_not_be_less_than_one() {
        assertThatThrownBy(() -> repo.findLatest(CHANNEL1, 0)).isInstanceOf(IllegalArgumentException.class);
    }
}
