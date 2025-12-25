package com.backend.backend_crud.service;

import com.backend.backend_crud.dto.request.SchoolRequest;
import com.backend.backend_crud.dto.request.UpdateSchoolRequest;
import com.backend.backend_crud.dto.response.PageResponse;
import com.backend.backend_crud.dto.response.SchoolResponse;

import java.util.List;

public interface SchoolService {
    PageResponse<SchoolResponse> getAll(int page, int size);
    SchoolResponse getById(Long id);
    SchoolResponse create(SchoolRequest request);
    SchoolResponse update(Long id, UpdateSchoolRequest request);
    void delete(Long id);
    PageResponse<SchoolResponse> searchSchoolsByName(String name, int page, int size);
}