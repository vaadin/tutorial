import {AppLayout} from "@vaadin/react-components/AppLayout.js";
import {DrawerToggle} from "@vaadin/react-components/DrawerToggle.js";
import Placeholder from "Frontend/components/placeholder/Placeholder.js";
import {useAuth} from "Frontend/auth";
import {Suspense} from "react";
import {Outlet, useLocation, useNavigate} from "react-router-dom";
import {SideNav} from "@vaadin/react-components/SideNav";
import {SideNavItem} from "@vaadin/react-components/SideNavItem";
import {Icon} from "@vaadin/react-components/Icon";
import {Scroller} from "@vaadin/react-components/Scroller";
import {Button} from "@vaadin/react-components/Button";
import {Tooltip} from "@vaadin/react-components/Tooltip";
import {computed, effect, signal} from "@vaadin/hilla-react-signals";

export const pageTitle = signal<string | undefined>(undefined);
const currentTitle = computed(() => pageTitle.value ?? "Vaadin Chat");

effect(() => {
    document.title = currentTitle.value;
});

export default function MainLayout() {
    const {state, logout} = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    return (
        <AppLayout primarySection="drawer">
            <span className="items-center flex text-l font-semibold h-xl px-m" slot="drawer">Vaadin Chat</span>
            <Scroller slot="drawer" className="p-s">
                <SideNav onNavigate={({path}) => navigate(path!)} location={location}>
                    <SideNavItem path="/">
                        <Icon icon="vaadin:building" slot="prefix"/>
                        Lobby
                    </SideNavItem>
                </SideNav>
            </Scroller>

            <header slot="navbar" className="items-center flex pe-m w-full">
                <DrawerToggle aria-label="Menu toggle">
                    <Tooltip slot="tooltip" text="Menu toggle"/>
                </DrawerToggle>
                <h2 className="text-l m-0 flex-grow">{currentTitle.value}</h2>
                <Button onClick={logout}>Logout {state.user?.name}</Button>
            </header>

            <Suspense fallback={<Placeholder/>}>
                <Outlet/>
            </Suspense>
        </AppLayout>
    );
}
