package com.diksha.config;

import com.diksha.entity.Role;
import com.diksha.enums.RoleType;
import com.diksha.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public RoleSeeder(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {

        for (RoleType roleType : RoleType.values()) {

            if (roleRepository.findByName(roleType).isEmpty()) {

                Role role = new Role();
                role.setName(roleType);

                roleRepository.save(role);
            }
        }
    }
}