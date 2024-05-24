package com.example.application.security;

import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.hilla.BrowserCallable;
import jakarta.annotation.security.PermitAll;

import java.util.Optional;

@BrowserCallable
@PermitAll
public class CurrentUser {

    private final AuthenticationContext authenticationContext;

    public CurrentUser(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    public Optional<User> get() {
        return authenticationContext
                .getPrincipalName()
                .map(name -> new User(name, authenticationContext.getGrantedRoles().toArray(new String[0])));
    }
}
