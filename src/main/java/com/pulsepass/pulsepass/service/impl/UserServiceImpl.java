package com.pulsepass.pulsepass.service.impl;

import com.pulsepass.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.pulsepass.dto.response.UserResponse;
import com.pulsepass.pulsepass.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Override
    public UserResponse register(RegisterUserRequest request) {
        throw new UnsupportedOperationException("UserServiceImpl.register not implemented yet");
    }

    @Override
    public UserResponse findByEmail(String email) {
        throw new UnsupportedOperationException("UserServiceImpl.findByEmail not implemented yet");
    }

    @Override
    public UserResponse findByUsername(String username) {
        throw new UnsupportedOperationException("UserServiceImpl.findByUsername not implemented yet");
    }
}
