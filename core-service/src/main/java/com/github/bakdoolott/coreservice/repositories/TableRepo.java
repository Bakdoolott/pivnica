package com.github.bakdoolott.coreservice.repositories;

import com.github.bakdoolott.coreservice.models.Tables;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TableRepo extends JpaRepository<Tables,Long> {
    List<Tables> findByHallIdAndEnableTrueOrderByTableNumberAsc(Long hallId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Tables t where t.id = :id")
    Optional<Tables> findByIdForUpdate(@Param("id") Long id);

}
