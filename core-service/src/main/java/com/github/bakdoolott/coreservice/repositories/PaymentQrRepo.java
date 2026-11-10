package com.github.bakdoolott.coreservice.repositories;

import com.github.bakdoolott.coreservice.models.PaymentQr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentQrRepo extends JpaRepository<PaymentQr, Long> {

    @Query("""
        select p from PaymentQr p
        where p.enable = true
        order by p.createdAt desc
        limit 1
    """)
    Optional<PaymentQr> findActive();

    @Query("""
        select p from PaymentQr p
        order by p.createdAt desc
    """)
    List<PaymentQr> findAllOrdered();

    @Modifying
    @Query("""
        update PaymentQr p
        set p.enable = false,
            p.updatedAt = :now,
            p.updatedBy = :adminId
        where p.enable = true
    """)
    int deactivateAll(@Param("now") java.time.LocalDateTime now,
                      @Param("adminId") Long adminId);
}