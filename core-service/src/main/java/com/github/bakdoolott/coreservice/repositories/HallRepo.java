package com.github.bakdoolott.coreservice.repositories;

import com.github.bakdoolott.coreservice.models.Hall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HallRepo extends JpaRepository<Hall,Long> {
}
