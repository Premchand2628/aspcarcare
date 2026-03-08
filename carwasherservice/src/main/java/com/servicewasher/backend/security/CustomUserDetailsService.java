package com.servicewasher.backend.security;

import com.servicewasher.backend.user.WasherUser;
import com.servicewasher.backend.repository.WasherUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final WasherUserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    WasherUser user = userRepository.findByEmail(email)
      .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    return User.builder()
      .username(user.getEmail())
      .password(user.getPassword())
      .disabled(!user.isEnabled())
      .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
      .build();
  }
}