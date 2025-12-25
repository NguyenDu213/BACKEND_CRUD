package com.backend.backend_crud.controller;

import com.backend.backend_crud.dto.request.SchoolRequest;
import com.backend.backend_crud.dto.request.UpdateSchoolRequest;
import com.backend.backend_crud.dto.response.ApiResponse;
import com.backend.backend_crud.dto.response.PageResponse;
import com.backend.backend_crud.dto.response.SchoolResponse;
import com.backend.backend_crud.service.SchoolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schools")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
public class SchoolController {

    private final SchoolService schoolService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SchoolResponse>>> getAllSchools(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size // Mặc định hiển thị 3
    ) {
        return ResponseEntity.ok(ApiResponse.<PageResponse<SchoolResponse>>builder()
                .status(true)
                .message("Lấy danh sách trường học thành công")
                .data(schoolService.getAll(page, size))
                .build());
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<SchoolResponse>>> searchSchools(
            @RequestParam("name") String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size
    ) {
        return ResponseEntity.ok(ApiResponse.<PageResponse<SchoolResponse>>builder()
                .status(true)
                .message("Kết quả tìm kiếm")
                .data(schoolService.searchSchoolsByName(name, page, size))
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SchoolResponse>> getSchoolById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<SchoolResponse>builder()
                .status(true)
                .message("Lấy chi tiết trường học thành công")
                .data(schoolService.getById(id))
                .build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SchoolResponse>> createSchool(@Valid @RequestBody SchoolRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<SchoolResponse>builder()
                .status(true)
                .message("Tạo trường học mới thành công")
                .data(schoolService.create(request))
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SchoolResponse>> updateSchool(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSchoolRequest request) {
        return ResponseEntity.ok(ApiResponse.<SchoolResponse>builder()
                .status(true)
                .message("Cập nhật thông tin trường thành công")
                .data(schoolService.update(id, request))
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSchool(@PathVariable Long id) {
        schoolService.delete(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(true)
                .message("Xóa trường học thành công")
                .build());
    }
}