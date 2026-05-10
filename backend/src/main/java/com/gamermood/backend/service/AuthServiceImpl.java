package com.gamermood.backend.service;

import com.gamermood.backend.dto.AuthResponseDto;
import com.gamermood.backend.dto.LoginRequestDto;
import com.gamermood.backend.dto.RegisterRequestDto;
import com.gamermood.backend.entity.Role;
import com.gamermood.backend.entity.User;
import com.gamermood.backend.exception.CredencialesInvalidasException;
import com.gamermood.backend.exception.EmailYaRegistradoException;
import com.gamermood.backend.repository.RoleRepository;
import com.gamermood.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public void register(RegisterRequestDto dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new EmailYaRegistradoException(dto.email());
        }

        if (dto.confirmPassword() != null && !dto.password().equals(dto.confirmPassword())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }

        Role rolUser = roleRepository.findByNombre("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));

        User user = new User();
        user.setUsername(dto.username());
        user.setEmail(dto.email());
        user.setPasswordHash(passwordEncoder.encode(dto.password()));
        user.setRoles(Set.of(rolUser));

        userRepository.save(user);
    }

    @Override
    public AuthResponseDto login(LoginRequestDto dto) {
        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(CredencialesInvalidasException::new);

        if (!passwordEncoder.matches(dto.password(), user.getPasswordHash())) {
            throw new CredencialesInvalidasException();
        }

        String token = jwtService.generarToken(user.getEmail());

        List<String> roles = user.getRoles().stream()
                .map(Role::getNombre)
                .toList();

        return new AuthResponseDto(token, user.getId(), user.getUsername(), user.getEmail(), roles);
    }
}
