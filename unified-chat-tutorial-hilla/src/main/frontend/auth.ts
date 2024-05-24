import {configureAuth} from '@vaadin/hilla-react-auth';
import {CurrentUser} from "Frontend/generated/endpoints";

const auth = configureAuth(CurrentUser.get);

export const useAuth = auth.useAuth;
export const AuthProvider = auth.AuthProvider;
