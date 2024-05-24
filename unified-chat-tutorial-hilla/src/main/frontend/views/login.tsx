import {useAuth} from "Frontend/auth";
import {LoginForm} from "@vaadin/react-components/LoginForm";
import {signal} from "@vaadin/hilla-react-signals";
import {ViewConfig} from "@vaadin/hilla-file-router/types.js";

export const config: ViewConfig = {
    title: "Login",
    loginRequired: false,
};

export default function Login() {
    const {login} = useAuth();
    const hasError = signal(false);

    return (
        <div className="flex flex-col h-full w-full items-center justify-center gap-m">
            <h1>Vaadin Chat</h1>
            <div>You can log in as 'alice', 'bob' or 'admin'. The password for all of them is 'password'.</div>
            <LoginForm error={hasError.value} onLogin={async ({detail: {username, password}}) => {
                const {defaultUrl, error, redirectUrl} = await login(username, password);
                if (error) {
                    hasError.value = true;
                } else {
                    const url = redirectUrl ?? defaultUrl ?? "/";
                    document.location = new URL(url, document.baseURI).pathname; // Because of https://github.com/vaadin/hilla/issues/2063
                }
            }}
            />
        </div>
    )
}
