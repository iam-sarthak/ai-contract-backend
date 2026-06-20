package com.contractai.auth.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AuthResponse {

    private String token;
    private String email;
    private List<String> roles;
}
