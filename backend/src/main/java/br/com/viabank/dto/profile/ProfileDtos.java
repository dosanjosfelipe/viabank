package br.com.viabank.dto.profile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;

public final class ProfileDtos {
    private ProfileDtos() {}

    public record ProfileResponse(
        Long id, String name, String cpf, String email, String phone,
        LocalDate birthDate, String address, String city, String state,
        String zipCode, Instant customerSince
    ) {}

    public record UpdateProfileRequest(
        @Size(max = 120) String name,
        @Email @Size(max = 160) String email,
        @Size(max = 20) String phone,
        @Size(max = 180) String address,
        @Size(max = 100) String city,
        @Size(max = 2) String state,
        @Size(max = 9) String zipCode
    ) {}
}
