package com.backend.backend_crud.service;

import com.backend.backend_crud.dto.request.UpdateUserRequest;
import com.backend.backend_crud.dto.request.UserRequest;
import com.backend.backend_crud.dto.response.ApiResponse;
import com.backend.backend_crud.dto.response.UserResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {
    ApiResponse<List<UserResponse>> getAll(Long userId);

    ApiResponse<UserResponse> createUser(UserRequest request, Long userId);
    ApiResponse<UserResponse> updateUser(UpdateUserRequest request, Long userId, Long updateBy);
    ApiResponse<UserResponse> deleteUser(Long id);

    ApiResponse<List<UserResponse>> searchUser(String search, Long userId);

    // Methods for role reassignment
    ApiResponse<List<UserResponse>> getUsersByRoleId(Long roleId, Long currentUserId);

    ApiResponse<Boolean> isRoleInUse(Long roleId);

    ApiResponse<String> reassignRole(Long oldRoleId, Long newRoleId, Long currentUserId);
}
