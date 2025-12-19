package com.backend.backend_crud.controller;

import com.backend.backend_crud.dto.request.UserRequest;
import com.backend.backend_crud.dto.response.ApiResponse;
import com.backend.backend_crud.dto.response.UserResponse;
import com.backend.backend_crud.service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAll(){
        return ResponseEntity.ok(userService.getAll());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid
            @RequestBody UserRequest request){
        return ResponseEntity.ok(userService.createUser(request));
    }
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @Valid
            @PathVariable Long id,
            @RequestBody UserRequest request){
        return ResponseEntity.ok(userService.updateUser(request, id));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> deleteUser(
            @Valid
            @PathVariable Long id){
        return ResponseEntity.ok(userService.deleteUser(id));
    }
}
