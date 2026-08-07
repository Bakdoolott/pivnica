package com.github.bakdoolott.coreservice.services;

import com.github.bakdoolott.coreservice.models.dto.request.CreateTableRequest;
import com.github.bakdoolott.coreservice.models.dto.request.UpdateTableRequest;
import com.github.bakdoolott.coreservice.models.dto.response.TableResponse;

import java.util.List;

public interface TableService {
    TableResponse create(CreateTableRequest request);
    List<TableResponse> update(List<UpdateTableRequest> request);
    TableResponse findById(Long id);
    List<TableResponse> findTablesByHallId(Long id);
    void deleteById(Long id);
}
