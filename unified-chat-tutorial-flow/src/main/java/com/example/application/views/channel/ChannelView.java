package com.example.application.views.channel;

import com.example.application.chat.ChatService;
import com.example.application.chat.Message;
import com.example.application.views.MainLayout;
import com.example.application.views.lobby.LobbyView;
import com.example.application.util.LimitedSortedAppendOnlyList;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import reactor.core.Disposable;

import java.util.Comparator;
import java.util.List;

import static com.vaadin.flow.theme.lumo.LumoUtility.Border;

@Route(value = "channel", layout = MainLayout.class)
@PermitAll
public class ChannelView extends VerticalLayout implements HasUrlParameter<String>, HasDynamicTitle {

    private static final int HISTORY_SIZE = 20; // A small number to demonstrate the feature
    private final ChatService chatService;
    private final MessageList messageList;
    private final LimitedSortedAppendOnlyList<Message> receivedMessages;
    private final String currentUserName;
    private String channelName;
    private String channelId;

    public ChannelView(ChatService chatService, AuthenticationContext authenticationContext) {
        this.chatService = chatService;
        this.currentUserName = authenticationContext.getPrincipalName().orElseThrow();
        setSizeFull();

        receivedMessages = new LimitedSortedAppendOnlyList<>(HISTORY_SIZE, Comparator.comparing(Message::sequenceNumber));

        messageList = new MessageList();
        messageList.addClassNames(Border.ALL);
        messageList.setSizeFull();
        add(messageList);

        var messageInput = new MessageInput(event -> sendMessage(event.getValue()));
        messageInput.setWidthFull();

        add(messageInput);
    }

    private Disposable subscribe() {
        var subscription = chatService
                .liveMessages(channelId)
                .subscribe(messages -> receiveMessages(messages, true));
        var lastSeenMessageId = receivedMessages.getLast().map(Message::messageId).orElse(null);
        receiveMessages(chatService.messageHistory(channelId, HISTORY_SIZE, lastSeenMessageId), false);
        return subscription;
    }

    private void sendMessage(String message) {
        if (!message.isBlank()) {
            chatService.postMessage(channelId, message);
        }
    }

    private void receiveMessages(List<Message> incoming, boolean notify) {
        getUI().ifPresent(ui -> ui.access(() -> {
            receivedMessages.addAll(incoming);
            messageList.setItems(receivedMessages.stream().map(this::createMessageListItem).toList());

            if (notify) {
                var messagesSentByOthers = incoming.stream().filter(m -> !m.author().equals(currentUserName)).toList();
                if (messagesSentByOthers.size() == 1) {
                    Notification.show("Received message from %s".formatted(messagesSentByOthers.getFirst().author()));
                } else if (messagesSentByOthers.size() > 1) {
                    Notification.show("Received %d messages".formatted(messagesSentByOthers.size()));
                }
            }
        }));
    }

    private MessageListItem createMessageListItem(Message message) {
        var item = new MessageListItem(message.message(), message.timestamp(), message.author());
        item.setUserColorIndex(Math.abs(message.author().hashCode() % 7));
        item.addClassNames(LumoUtility.Margin.SMALL, LumoUtility.BorderRadius.MEDIUM);
        if (message.author().equals(currentUserName)) {
            item.addClassNames(LumoUtility.Background.CONTRAST_5);
        }
        return item;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        var subscription = subscribe();
        addDetachListener(event -> subscription.dispose());
    }

    @Override
    public void setParameter(BeforeEvent event, String channelId) {
        this.channelId = channelId;
        
        chatService.channel(channelId).ifPresentOrElse(
                channel -> this.channelName = channel.name(),
                () -> event.forwardTo(LobbyView.class)
        );
    }

    @Override
    public String getPageTitle() {
        return channelName;
    }
}
