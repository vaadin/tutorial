import {ChatService} from "Frontend/generated/endpoints";
import {useAuth} from "Frontend/auth";
import {TextField} from "@vaadin/react-components/TextField";
import {Button} from "@vaadin/react-components/Button";
import {Notification} from "@vaadin/react-components/Notification";
import {Link} from "react-router-dom";
import {signal, useSignal} from "@vaadin/hilla-react-signals";
import {useEffect} from "react";
import Channel from "Frontend/generated/com/example/application/chat/Channel";
import {VirtualList} from "@vaadin/react-components/VirtualList";
import {Avatar} from "@vaadin/react-components/Avatar";
import {formatDate, hashCode} from "Frontend/util/util";

import {ViewConfig} from "@vaadin/hilla-file-router/types.js";
import {pageTitle} from "Frontend/views/@layout";

export const config: ViewConfig = {
    title: "Channels",
    loginRequired: true,
}

const channels = signal<Channel[]>([]);

function AddChannelComponent() {
    const name = useSignal('');
    const buttonDisabled = useSignal(false);

    const addChannel = async () => {
        if (name.value !== '') {
            buttonDisabled.value = true;
            try {
                const newChannel = await ChatService.createChannel(name.value);
                name.value = '';
                channels.value = [...channels.value, newChannel];
                Notification.show("Channel successfully created", {
                    theme: "success",
                    position: "bottom-end"
                });
            } catch (err) {
                console.error("Error creating channel", err);
                Notification.show("Failed to create channel. Please try again later.", {
                    theme: "error",
                    position: "bottom-end"
                });
            } finally {
                buttonDisabled.value = false;
            }
        }
    };

    // TODO Assign ENTER as shortcut key to button
    return <div className="w-full flex flex-row gap-s">
        <TextField placeholder="New channel name" className="flex-grow" value={name.value}
                   onChange={e => name.value = e.target.value}/>
        <Button theme="primary" onClick={addChannel} disabled={buttonDisabled.value}>Add channel</Button>
    </div>
}

function truncateMessage(msg: string) {
    if (msg.length > 50) {
        return msg.substring(0, 50) + "...";
    }
    return msg;
}

function ChannelComponent({channel}: { channel: Channel }) {
    const colorIndex = Math.abs(hashCode(channel.id) % 7);
    return <div className="flex gap-m p-m rounded-m channel" key={"channel-" + channel.id}>
        <Avatar name={channel.name} theme="small" colorIndex={colorIndex}/>
        <div className="flex-auto flex flex-col leading-xs gap-xs">
            <div className="flex items-baseline justify-start gap-s">
                <Link to={"channel/" + channel.id} className="text-m font-bold text-body">{channel.name}</Link>
                {channel.lastMessage && <div className="text-s text-secondary">
                    {formatDate(new Date(channel.lastMessage.timestamp))}
                </div>}
            </div>
            {channel.lastMessage && <div className="text-s text-secondary"><span
                className="font-bold">{channel.lastMessage.author}</span>: {truncateMessage(channel.lastMessage.message)}
            </div>}
            {!channel.lastMessage && <div className="text-s text-secondary">No messages</div>}
        </div>
    </div>
}

export default function LobbyView() {
    const {hasAccess} = useAuth();
    const isAdmin = hasAccess({rolesAllowed: ["ADMIN"]});
    pageTitle.value = "Lobby";

    useEffect(() => {
        (async () => {
            try {
                channels.value = await ChatService.channels();
            } catch (err) {
                console.error("Error loading channels", err);
                Notification.show("Failed to load channels. Please try again later.", {
                    theme: "error",
                    position: "middle"
                });
                channels.value = [];
            }
        })();
    }, []);

    return (<div className="flex flex-col gap-s box-border p-m lobby-view h-full w-full">
        <VirtualList items={channels.value} className="flex-grow border p-s">
            {({item}) => {
                return <ChannelComponent channel={item}/>
            }}
        </VirtualList>
        {isAdmin && <AddChannelComponent/>}
    </div>);
}