package vn.tt.practice.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    // POST : /auth/register
    // POST : /auth/login
    // POST : /auth/logout
    // POST : /auth/refresh-token

    // GET : /users/me
    // PUT : /users/me
    // GET : /users/{id}
    // PUT : /users
}

