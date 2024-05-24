package com.example.application.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class CurrentUserTest {

    @Autowired
    CurrentUser currentUser;

    @Test
    @WithAnonymousUser
    @DisplayName("Anonymous users are regarded as not logged in")
    public void anonymous_users_are_regarded_as_not_logged_in() {
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isInstanceOf(AnonymousAuthenticationToken.class);
        assertThat(currentUser.get()).isEmpty();
    }

    @Test
    @WithMockUser(authorities = {"ROLE_USER", "AUTH_NOT_A_ROLE", "ROLE_ADMIN"})
    @DisplayName("Only roles are included in the user object and their prefixes are stripped")
    public void only_roles_are_included_in_the_user_object() {
        assertThat(currentUser.get()).hasValueSatisfying(user -> assertThat(user.roles()).containsExactlyInAnyOrder("USER", "ADMIN"));
    }

    @Test
    @WithMockUser(username = "joecool")
    @DisplayName("The username is included in the user object")
    public void username_is_included_in_the_user_object() {
        assertThat(currentUser.get()).hasValueSatisfying(user -> assertThat(user.name()).isEqualTo("joecool"));
    }
}
