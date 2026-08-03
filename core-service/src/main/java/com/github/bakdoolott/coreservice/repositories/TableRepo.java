package com.github.bakdoolott.coreservice.repositories;

import com.github.bakdoolott.coreservice.models.Tables;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TableRepo extends JpaRepository<Tables,Long> {
    List<Tables> findByHallIdAndEnableTrueOrderByTableNumberAsc(Long hallId);


}
