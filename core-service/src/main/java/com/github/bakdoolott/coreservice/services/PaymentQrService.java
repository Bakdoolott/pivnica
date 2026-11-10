package com.github.bakdoolott.coreservice.services;

import com.github.bakdoolott.coreservice.models.dto.request.UpdatePaymentQrDto;
import com.github.bakdoolott.coreservice.models.dto.response.PaymentQrForBookingResponse;
import com.github.bakdoolott.coreservice.models.dto.response.PaymentQrResponse;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PaymentQrService {

    Optional<PaymentQrResponse> getActive();
    List<PaymentQrResponse> getAll();

    /** Для гостя: QR + сумма из его брони. */
    PaymentQrForBookingResponse getForBooking(Long userId, Long bookingId);

    PaymentQrResponse upload(Long adminId, MultipartFile file, String description);
    PaymentQrResponse createFromUrl(Long adminId, String imageUrl, String description);

    PaymentQrResponse update(Long adminId, Long id, UpdatePaymentQrDto dto);
    void disable(Long adminId, Long id);
    PaymentQrResponse activate(Long adminId, Long id);
}