package com.diksha.controller;

import com.diksha.dto.CreateAdminRequest;
import com.diksha.dto.CurrentUserResponse;
import com.diksha.service.AdminManagementService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Mirrors the reference Python backend's POST /api/users/admins — only an existing Admin can create another Admin. */
@RestController
@RequestMapping("/api/admin/admins")
@CrossOrigin("*")
public class AdminManagementController {

    private final AdminManagementService adminManagementService;

    public AdminManagementController(AdminManagementService adminManagementService) {
        this.adminManagementService = adminManagementService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public CurrentUserResponse createAdmin(@RequestBody CreateAdminRequest request) {
        return adminManagementService.createAdmin(request);
    }
}
