package br.com.viabank.dto.auth;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record RegisterRequest(
    @NotBlank @Size(max = 120) String name,
    @NotBlank @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 dígitos") String cpf,
    @NotBlank @Email @Size(max = 160) String email,
    @NotBlank @Pattern(regexp = "\\d{10,15}", message = "Telefone inválido") String phone,
    @NotNull @Past LocalDate birthDate,
    @NotBlank @Size(min = 6, max = 72) String password
) {}
