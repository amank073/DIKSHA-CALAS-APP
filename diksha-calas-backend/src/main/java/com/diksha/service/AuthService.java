package com.diksha.service;

import com.diksha.dto.AuthResponse;
import com.diksha.dto.CurrentUserResponse;
import com.diksha.dto.LoginRequest;
import com.diksha.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    CurrentUserResponse me(String email);

}