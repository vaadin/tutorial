import {signal} from "@preact/signals-react";
import connectClient from "Frontend/generated/connect-client.default";
import {State} from "@vaadin/hilla-frontend";

export const connectionActive = signal(connectClient.fluxConnection.state == State.ACTIVE);

// TODO This should be a part of the framework
connectClient.fluxConnection.addEventListener('state-changed', (event: CustomEvent<{ active: boolean }>) => {
    connectionActive.value = event.detail.active;
});
