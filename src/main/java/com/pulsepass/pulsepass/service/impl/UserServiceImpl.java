package com.pulsepass.pulsepass.service.impl;

import com.pulsepass.pulsepass.domain.User;
import com.pulsepass.pulsepass.domain.UserCredential;
import com.pulsepass.pulsepass.domain.UserProfile;
import com.pulsepass.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.pulsepass.dto.response.UserResponse;
import com.pulsepass.pulsepass.enums.RoleName;
import com.pulsepass.pulsepass.exception.BusinessRuleException;
import com.pulsepass.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.pulsepass.mapper.UserMapper;
import com.pulsepass.pulsepass.repository.UserRepository;
import com.pulsepass.pulsepass.repository.UserCredentialRepository;
import com.pulsepass.pulsepass.service.UserService;
import java.time.LocalDate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserCredentialRepository credentialRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           UserCredentialRepository credentialRepository,
                           UserMapper userMapper,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserResponse register(RegisterUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username already exists: " + request.username());
        }
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("Email already exists: " + request.email());
        }
        if (request.birthDate() != null && request.birthDate().isAfter(LocalDate.now())) {
            throw new BusinessRuleException("Birth date cannot be in the future.");
        }

        User user = new User(request.username(), request.email());
        user.getRoles().clear();
        user.addRole(RoleName.USER);

        UserProfile profile = new UserProfile(request.firstName(), request.lastName(), request.phone(),
                request.city(), request.birthDate(), user);
        user.setProfile(profile);
        User savedUser = userRepository.save(user);
        credentialRepository.save(new UserCredential(savedUser, passwordEncoder.encode(request.password())));
        return userMapper.toResponse(savedUser);
    }

    @Override
    public UserResponse findByEmail(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse findByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return userMapper.toResponse(user);
    }
}
