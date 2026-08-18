package com.diksha.service;

import com.diksha.dto.CreateAdminRequest;
import com.diksha.dto.CurrentUserResponse;

public interface AdminManagementService {

    CurrentUserResponse createAdmin(CreateAdminRequest request);
}
