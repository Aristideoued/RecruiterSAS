package com.recruitersaas.service;

import com.recruitersaas.dto.request.CreateAdminRequest;
import com.recruitersaas.dto.request.UpdateAdminRequest;
import com.recruitersaas.dto.response.AdminUserResponse;
import com.recruitersaas.dto.response.PageResponse;

public interface AdminUserService {

    PageResponse<AdminUserResponse> getAllAdmins(int page, int size);

    AdminUserResponse createAdmin(CreateAdminRequest request);

    AdminUserResponse updateAdmin(String id, UpdateAdminRequest request);

    void toggleAdmin(String id);

    void deleteAdmin(String id);
}
