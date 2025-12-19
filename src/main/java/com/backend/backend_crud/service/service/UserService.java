package com.backend.backend_crud.service.service;

import com.backend.backend_crud.dto.request.UserRequest;
import com.backend.backend_crud.dto.response.ApiResponse;
import com.backend.backend_crud.dto.response.UserResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {
    ApiResponse<List<UserResponse>> getAll();
    ApiResponse<UserResponse> createUser(UserRequest request);
    ApiResponse<UserResponse> updateUser(UserRequest request, Long id);
    ApiResponse<UserResponse> deleteUser(Long id);

}
