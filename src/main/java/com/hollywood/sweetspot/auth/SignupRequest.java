package com.hollywood.sweetspot.auth;

import java.util.Map;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


public record SignupRequest(@Email String email, @NotBlank String password, String name, Map<String, Boolean> agreements) {}