package com.pulsepass.pulsepass.service.impl;

import com.pulsepass.pulsepass.domain.User;
import com.pulsepass.pulsepass.domain.UserCredential;
import com.pulsepass.pulsepass.repository.UserCredentialRepository;
import com.pulsepass.pulsepass.repository.UserRepository;
import com.pulsepass.pulsepass.security.UserPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserCredentialRepository credentialRepository;

    public UserDetailsServiceImpl(UserRepository userRepository, UserCredentialRepository credentialRepository) {
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserCredential credential = credentialRepository.findWithUserByUsername(username)
                .or(() -> credentialRepository.findWithUserByEmailIgnoreCase(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return UserPrincipal.create(credential.getUser(), credential.getPasswordHash());
    }

    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));
        return UserPrincipal.create(user);
    }
}
