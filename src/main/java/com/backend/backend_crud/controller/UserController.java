package com.backend.backend_crud.controller;

import com.backend.backend_crud.dto.request.UserRequest;
import com.backend.backend_crud.dto.response.ApiResponse;
import com.backend.backend_crud.dto.response.UserResponse;
import com.backend.backend_crud.service.UserService;
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
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAll(
            @RequestParam Long userId,
            @RequestParam(required = false) Long schoolId){
        return ResponseEntity.ok(userService.getAll(userId, schoolId));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserResponse>>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long schoolId,
            @RequestParam Long userId){
        return ResponseEntity.ok(userService.searchUser(keyword, userId));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @RequestParam Long userId,
            @Valid
            @RequestBody UserRequest request){
        return ResponseEntity.ok(userService.createUser(request, userId));
    }
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @PathVariable Long updateBy,
            @Valid
            @RequestBody UserRequest request){
        return ResponseEntity.ok(userService.updateUser(request, id, updateBy));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> deleteUser(
            @Valid
            @PathVariable Long id){
        return ResponseEntity.ok(userService.deleteUser(id));
    }
}
