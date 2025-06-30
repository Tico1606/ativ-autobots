package com.autobots.automanager.security;

import com.autobots.automanager.entities.User;
import com.autobots.automanager.repositories.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  public CustomUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user = userRepository.findByEmailsEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

    String password = user.getCredentialUserPassword().getPassword();
    String role = user.getRole().name();

    return org.springframework.security.core.userdetails.User
        .withUsername(email)
        .password(password)
        .authorities("ROLE_" + role)
        .build();
  }
}
