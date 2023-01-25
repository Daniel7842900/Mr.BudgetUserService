package com.api.mrbudget.userservice.security;

import com.api.mrbudget.userservice.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class JwtUtilTest {
    private JwtUtil jwtUtil;
    private String secret;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("user@test.com");
        secret = "onlyTheTestKnows";
        jwtUtil = new JwtUtil(secret);
    }

    @Test
    void generateToken() {
        String token = jwtUtil.generateToken(user.getEmail());
        String decoded = jwtUtil.getUserEmailFromJwtToken(token);
        assertThat(decoded, equalTo(user.getEmail()));
    }

    @Test
    void getUserEmailFromJwtToken() {
    }
}