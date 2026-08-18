package com.diksha.service.impl;

import com.diksha.dto.CreateAdminRequest;
import com.diksha.dto.CurrentUserResponse;
import com.diksha.entity.Role;
import com.diksha.entity.User;
import com.diksha.enums.RoleType;
import com.diksha.repository.RoleRepository;
import com.diksha.repository.UserRepository;
import com.diksha.service.AdminManagementService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminManagementServiceImpl
        implements AdminManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminManagementServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public CurrentUserResponse createAdmin(CreateAdminRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Role adminRole = roleRepository.findByName(RoleType.ADMIN)
                .orElseThrow(() ->
                        new RuntimeException("ADMIN role not found"));

        User admin = new User();

        admin.setFirstName(request.getFirstName());
        admin.setLastName(request.getLastName());
        admin.setEmail(request.getEmail());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setPhone(request.getPhone());
        admin.setRole(adminRole);
        admin.setEnabled(true);

        User saved = userRepository.save(admin);

        return new CurrentUserResponse(
                saved.getId(),
                saved.getFirstName(),
                saved.getLastName(),
                saved.getEmail(),
                saved.getPhone(),
                RoleType.ADMIN
        );
    }
}
