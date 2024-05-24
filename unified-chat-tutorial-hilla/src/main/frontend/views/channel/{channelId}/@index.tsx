import {useNavigate, useParams} from "react-router-dom";
import {useEffect} from "react";
import {ChatService} from "Frontend/generated/endpoints";
import {VerticalLayout} from "@vaadin/react-components/VerticalLayout";
import {MessageList} from "@vaadin/react-components/MessageList";
import {MessageInput} from "@vaadin/react-components/MessageInput";
import {Subscription} from "@vaadin/hilla-frontend";
import {useAuth} from "Frontend/auth";
import {useSignal} from "@preact/signals-react";
import Message from "Frontend/generated/com/example/application/chat/Message";
import {connectionActive} from "Frontend/util/workarounds";
import {Notification} from "@vaadin/react-components/Notification";
import Channel from "Frontend/generated/com/example/application/chat/Channel";
import {formatDate, hashCode} from "Frontend/util/util";
import {Button} from "@vaadin/react-components/Button";

import {ViewConfig} from "@vaadin/hilla-file-router/types.js";
import {pageTitle} from "Frontend/views/@layout";

export const config: ViewConfig = {
    loginRequired: true,
}

const HISTORY_SIZE = 20; // A small number to demonstrate the feature

export default function ChannelView() {
    const currentUserName = useAuth().state.user?.name;
    const navigate = useNavigate();
    const params = useParams();
    const channel = useSignal<Channel | undefined>(undefined);
    const subscription = useSignal<Subscription<Message[]> | undefined>(undefined);
    const messages = useSignal<Message[]>([]);
    const error = useSignal(false);

    function subscribe() {
        (async () => {
            if (!channel.value) {
                return;
            }
            unsubscribe();
            error.value = false;
            console.log("Subscribing to channel", channel.value.id);
            subscription.value = ChatService.liveMessages(channel.value.id)
                .onNext(incoming => receiveMessages(incoming, true))
                .onError(() => {
                    console.error("Error in message subscription", subscription);
                    error.value = true;
                });
            try {
                const lastSeenMessageId = messages.value.length == 0 ? undefined : messages.value[messages.value.length - 1].messageId;
                console.log("Fetching messages sent after", lastSeenMessageId);
                receiveMessages(await ChatService.messageHistory(channel.value.id, HISTORY_SIZE, lastSeenMessageId));
            } catch (err) {
                console.error("Error fetching message history", err);
                error.value = true;
            }
        })();
    }

    function receiveMessages(incoming: Message[], notify: boolean = false) {
        console.log("Received messages", incoming);
        const newMessages = [...messages.value, ...incoming].sort((a, b) => a.sequenceNumber - b.sequenceNumber);
        if (newMessages.length > HISTORY_SIZE) {
            newMessages.splice(0, newMessages.length - HISTORY_SIZE);
        }
        messages.value = newMessages;

        if (notify) {
            const messagesSentByOthers = incoming.filter(m => m.author !== currentUserName);
            if (messagesSentByOthers.length == 1) {
                Notification.show(`Received message from ${messagesSentByOthers[0].author}`);
            } else if (messagesSentByOthers.length > 1) {
                Notification.show(`Received ${messagesSentByOthers.length} messages`);
            }
        }
    }

    async function sendMessage(message: string) {
        if (!channel.value) {
            return;
        }
        try {
            await ChatService.postMessage(channel.value.id, message);
        } catch (_) {
            Notification.show("Failed to send the message. Please try again later.", {
                theme: "error",
                position: "bottom-end"
            });
        }
    }

    function unsubscribe() {
        if (subscription.value) {
            console.log("Unsubscribing from channel", subscription);
            subscription.value.cancel();
            subscription.value = undefined;
        }
    }

    async function updateChannel() {
        channel.value = params.channelId ? await ChatService.channel(params.channelId) : undefined;
        if (!channel.value) {
            navigate("/");
        } else {
            pageTitle.value = channel.value.name;
        }
    }

    useEffect(() => {
        updateChannel().catch(console.error);
    }, [params.channelId]);

    useEffect(() => {
        if (connectionActive.value) {
            subscribe();
        } else {
            unsubscribe();
            console.error("Connection to server lost");
            error.value = true;
        }
        return unsubscribe;
    }, [channel.value, connectionActive.value]); // TODO There should be a framework-provided way of checking the state of a subscription and re-subscribing when the connection is lost

    return (
        <VerticalLayout theme={"padding spacing"} className={"w-full h-full channel-view"}>
            <MessageList className={"w-full h-full border"} items={messages.value.map(message => ({
                text: message.message,
                userName: message.author,
                time: formatDate(new Date(message.timestamp)),
                theme: message.author === currentUserName ? "current-user" : undefined,
                userColorIndex: Math.abs(hashCode(message.author) % 7)
            }))}/>
            <MessageInput className={"w-full"} onSubmit={e => sendMessage(e.detail.value)}/>
            <Notification opened={error.value} theme={"error"} duration={0}>
                <span>There is a problem with the chat. Please reload the page.</span>
                <Button onClick={_ => window.location.reload()}>Reload</Button>
            </Notification>
        </VerticalLayout>
    );
}