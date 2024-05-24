/******************************************************************************
 * This file is auto-generated by Vaadin.
 * It configures React Router automatically by looking for React views files,
 * located in `src/main/frontend/views/` directory.
 * A manual configuration can be done as well, you have to:
 * - copy this file or create your own `routes.tsx` in your frontend directory,
 *   then modify this copied/created file. By default, the `routes.tsx` file
 *   should be in `src/main/frontend/` folder;
 * - use `RouterConfigurationBuilder` API to configure routes for the application;
 * - restart the application, so that the imports get re-generated.
 *
 * `RouterConfigurationBuilder` combines a File System-based route configuration
 * or your explicit routes configuration with the server-side routes.
 *
 * It has the following methods:
 * - `withFileRoutes` enables the File System-based routes autoconfiguration;
 * - `withReactRoutes` adds manual explicit route hierarchy. Allows also to add
 * an individual route, which then merged into File System-based routes,
 * e.g. Log In view;
 * - `withFallback` adds a given component, e.g. server-side routes,
 * to each branch of the current list of routes;
 * - `protect` optional method that adds an authentication later to the routes.
 * May be used with no parameters or with a path to redirect to, if the user is
 * not authenticated.
 * - `build` terminal build operation that returns the final routes array
 * RouterObject[] and router object.
 *
 * NOTE:
 * - You need to restart the dev-server after adding the new `routes.tsx` file.
 * After that, all modifications to `routes.tsx` are recompiled automatically.
 * - You may need to change a routes import in `index.tsx`, if `index.tsx`
 * exists in the frontend folder (not in generated folder) and you copied the file,
 * as the import isn't updated automatically by Vaadin in this case.
 ******************************************************************************/
import { RouterConfigurationBuilder } from '@vaadin/hilla-file-router/runtime.js';
import fileRoutes from 'Frontend/generated/file-routes.js';

export const { router, routes } = new RouterConfigurationBuilder()
    .withFileRoutes(fileRoutes) // (1)
    // To define routes manually or adding an individual route, use the
    // following code and remove (1):
    // .withReactRoutes(
    //     {
    //         element: <MainLayout />,
    //         handle: { title: 'Main' },
    //         children: [
    //             { path: '/hilla', element: <HillaView />, handle: { title: 'Hilla' } }
    //         ],
    //     },
    //     { path: '/login', element: <Login />, handle: { title: 'Login' } }
    // )
    // OR
    // .withReactRoutes(
    //     { path: '/login', element: <Login />, handle: { title: 'Login' } },
    // )
    //.withFallback(Flow)
    // Optional method that adds an authentication for routes.
    // Can take an optional path to redirect to, if not authenticated:
    // .protect('/login');
    .protect()
    .build();
