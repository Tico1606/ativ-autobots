package com.autobots.automanager.dtos;

import lombok.Data;

@Data
public class AuthRequest {
  private String email;
  private String password;
}
