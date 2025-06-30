package com.autobots.automanager.controllers;

import com.autobots.automanager.entities.User;
import com.autobots.automanager.enums.UserRole;
import com.autobots.automanager.entities.Company;
import com.autobots.automanager.repositories.CompanyRepository;
import com.autobots.automanager.repositories.UserRepository;
import com.autobots.automanager.services.AddLinkUser;
import com.autobots.automanager.services.UpdateUser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private CompanyRepository companyRepository;

  @Autowired
  private AddLinkUser addLink;

  @Autowired
  private UpdateUser updateUser;

  @Autowired
  private PasswordEncoder passwordEncoder;

  private boolean canManagerOperate(User user) {
    UserRole role = user.getRole();
    return role == UserRole.GERENTE || role == UserRole.VENDEDOR || role == UserRole.CLIENTE;
  }

  @PreAuthorize("hasAnyRole('GERENTE', 'ADMIN')")
  @GetMapping("/list")
  public ResponseEntity<List<User>> listUsers() {
    List<User> users = userRepository.findAll();
    if (users.isEmpty()) {
      return ResponseEntity.noContent().build();
    }
    addLink.addLink(new HashSet<>(users));
    return ResponseEntity.ok(users);
  }

  @PreAuthorize("hasAnyRole('GERENTE', 'ADMIN', 'VENDEDOR', 'CLIENTE')")
  @GetMapping("/{id}")
  public ResponseEntity<User> getUser(@PathVariable Long id) {
    Optional<User> userOpt = userRepository.findById(id);
    if (userOpt.isEmpty())
      return ResponseEntity.notFound().build();

    User user = userOpt.get();
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String loggedUsername = auth.getName();

    boolean isCustomer = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));
    boolean isSeller = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_VENDEDOR"));
    boolean isManager = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_GERENTE"));

    if (isCustomer && !user.getCredentialUserPassword().getUsername().equals(loggedUsername)) {
      return ResponseEntity.status(403).build();
    }

    if (isSeller && user.getRole() != UserRole.CLIENTE) {
      return ResponseEntity.status(403).build();
    }

    if (isManager && !canManagerOperate(user)) {
      return ResponseEntity.status(403).build();
    }

    addLink.addLink(user);
    return ResponseEntity.ok(user);
  }

  @PreAuthorize("hasAnyRole('GERENTE', 'ADMIN')")
  @PostMapping("/create")
  public ResponseEntity<?> createUser(@RequestBody User user) {
    UserRole role = user.getRole();
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    boolean isManager = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_GERENTE"));
    boolean isSeller = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_VENDEDOR"));

    if (isManager && !canManagerOperate(user)) {
      return ResponseEntity.status(403).body("Manager is not allowed to create users with role: " + role);
    }

    if (isSeller && role != UserRole.CLIENTE) {
      return ResponseEntity.status(403).body("Seller can only create customers");
    }

    if (user.getCompany() == null || user.getCompany().getId() == null) {
      return ResponseEntity.badRequest().body("Company is required");
    }

    Optional<Company> companyOpt = companyRepository.findById(user.getCompany().getId());
    if (companyOpt.isEmpty()) {
      return ResponseEntity.badRequest().body("Company not found");
    }

    user.setCompany(companyOpt.get());

    String rawPassword = user.getCredentialUserPassword().getPassword();
    user.getCredentialUserPassword().setPassword(passwordEncoder.encode(rawPassword));

    User newUser = userRepository.save(user);
    addLink.addLink(newUser);
    return ResponseEntity.ok(newUser);
  }

  @PreAuthorize("hasAnyRole('GERENTE', 'ADMIN', 'VENDEDOR')")
  @PutMapping("/update/{id}")
  public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
    Optional<User> userOptional = userRepository.findById(id);
    if (userOptional.isEmpty())
      return ResponseEntity.notFound().build();

    User user = userOptional.get();
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    boolean isManager = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_GERENTE"));
    boolean isSeller = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_VENDEDOR"));

    if (isManager && !canManagerOperate(user)) {
      return ResponseEntity.status(403).build();
    }

    if (isSeller && user.getRole() != UserRole.CLIENTE) {
      return ResponseEntity.status(403).build();
    }

    updateUser.update(user, updatedUser);
    User savedUser = userRepository.save(user);
    addLink.addLink(savedUser);
    return ResponseEntity.ok(savedUser);
  }

  @PreAuthorize("hasAnyRole('GERENTE', 'ADMIN', 'VENDEDOR')")
  @DeleteMapping("/delete/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    Optional<User> userOpt = userRepository.findById(id);
    if (userOpt.isEmpty())
      return ResponseEntity.notFound().build();

    User user = userOpt.get();
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    boolean isManager = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_GERENTE"));
    boolean isSeller = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_VENDEDOR"));

    if (isManager && !canManagerOperate(user)) {
      return ResponseEntity.status(403).build();
    }

    if (isSeller && user.getRole() != UserRole.CLIENTE) {
      return ResponseEntity.status(403).build();
    }

    userRepository.delete(user);
    return ResponseEntity.noContent().build();
  }
}
