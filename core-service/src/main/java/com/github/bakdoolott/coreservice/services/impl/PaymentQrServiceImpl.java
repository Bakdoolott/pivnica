package com.github.bakdoolott.coreservice.services.impl;


import com.github.bakdoolott.coreservice.config.BookingProperties;
import com.github.bakdoolott.coreservice.exceptions.LogicException;
import com.github.bakdoolott.coreservice.exceptions.NotFoundException;
import com.github.bakdoolott.coreservice.mappers.PaymentQrMapper;
import com.github.bakdoolott.coreservice.models.Booking;
import com.github.bakdoolott.coreservice.models.PaymentQr;
import com.github.bakdoolott.coreservice.models.dto.request.UpdatePaymentQrDto;
import com.github.bakdoolott.coreservice.models.dto.response.PaymentQrForBookingResponse;
import com.github.bakdoolott.coreservice.models.dto.response.PaymentQrResponse;
import com.github.bakdoolott.coreservice.models.enums.BookingStatus;
import com.github.bakdoolott.coreservice.repositories.BookingRepo;
import com.github.bakdoolott.coreservice.repositories.PaymentQrRepo;
import com.github.bakdoolott.coreservice.services.PaymentQrService;
import com.github.bakdoolott.coreservice.services.PaymentQrStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class PaymentQrServiceImpl implements PaymentQrService {

    private final PaymentQrRepo repo;
    private final PaymentQrStorage storage;
    private final PaymentQrMapper mapper;
    private final BookingProperties bookingProperties;
    private final BookingRepo bookingRepo;

    public PaymentQrServiceImpl(PaymentQrRepo repo,
                                PaymentQrStorage storage,
                                PaymentQrMapper mapper,
                                BookingProperties bookingProperties,
                                BookingRepo bookingRepo) {
        this.repo = repo;
        this.storage = storage;
        this.mapper = mapper;
        this.bookingProperties = bookingProperties;
        this.bookingRepo = bookingRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentQrResponse> getActive() {
        return repo.findActive().map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentQrResponse> getAll() {
        return repo.findAllOrdered().stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentQrForBookingResponse getForBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepo.findOwnById(bookingId, userId)
                .orElseThrow(() -> new NotFoundException("Бронь не найдена"));

        if (booking.getBookingStatus() != BookingStatus.PENDING
                && booking.getBookingStatus() != BookingStatus.AWAITING_CONFIRMATION) {
            throw new LogicException("Оплата доступна только для неоплаченных броней");
        }

        PaymentQr qr = repo.findActive()
                .orElseThrow(() -> new LogicException("QR для оплаты не настроен, обратитесь в клуб"));

        return new PaymentQrForBookingResponse(
                booking.getId(),
                qr.getImageUrl(),
                qr.getDescription(),
                booking.getTotalAmount(),
                qr.getMessengerChatUrl()    // ← передаём ссылку
        );
    }

    @Override
    @Transactional
    public PaymentQrResponse upload(Long adminId, MultipartFile file, String description, String messengerChatUrl) {
        validateFile(file);
        LocalDateTime now = LocalDateTime.now(bookingProperties.getClubZone());
        repo.deactivateAll(now, adminId);

        String url = storage.save(file);

        PaymentQr qr = new PaymentQr();
        qr.setImageUrl(url);
        qr.setDescription(description != null ? description.trim() : null);
        qr.setMessengerChatUrl(messengerChatUrl != null ? messengerChatUrl.trim() : null); // ←
        qr.setEnable(true);
        qr.setCreatedAt(now);
        qr.setCreatedBy(adminId);

        return mapper.toResponse(repo.save(qr));
    }

    @Override
    @Transactional
    public PaymentQrResponse createFromUrl(Long adminId, String imageUrl, String description, String messengerChatUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new LogicException("URL обязателен");
        }
        LocalDateTime now = LocalDateTime.now(bookingProperties.getClubZone());
        repo.deactivateAll(now, adminId);

        PaymentQr qr = new PaymentQr();
        qr.setImageUrl(imageUrl.trim());
        qr.setDescription(description != null ? description.trim() : null);
        qr.setMessengerChatUrl(messengerChatUrl != null ? messengerChatUrl.trim() : null); // ←
        qr.setEnable(true);
        qr.setCreatedAt(now);
        qr.setCreatedBy(adminId);

        return mapper.toResponse(repo.save(qr));
    }

    @Override
    @Transactional
    public PaymentQrResponse update(Long adminId, Long id, UpdatePaymentQrDto dto) {
        PaymentQr qr = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("QR не найден"));

        if (dto.getDescription() != null) {
            String trimmed = dto.getDescription().trim();
            qr.setDescription(trimmed.isEmpty() ? null : trimmed);
        }
        if (dto.getMessengerChatUrl() != null) {   // ← добавить
            String trimmed = dto.getMessengerChatUrl().trim();
            qr.setMessengerChatUrl(trimmed.isEmpty() ? null : trimmed);
        }
        qr.setUpdatedAt(LocalDateTime.now(bookingProperties.getClubZone()));
        qr.setUpdatedBy(adminId);

        return mapper.toResponse(repo.save(qr));
    }

    @Override
    @Transactional
    public void disable(Long adminId, Long id) {
        PaymentQr qr = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("QR не найден"));
        qr.setEnable(false);
        qr.setUpdatedAt(LocalDateTime.now(bookingProperties.getClubZone()));
        qr.setUpdatedBy(adminId);
        repo.save(qr);
        log.info("Payment QR disabled: id={}, adminId={}", id, adminId);
    }

    @Override
    @Transactional
    public PaymentQrResponse activate(Long adminId, Long id) {
        PaymentQr qr = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("QR не найден"));

        LocalDateTime now = LocalDateTime.now(bookingProperties.getClubZone());
        repo.deactivateAll(now, adminId);

        qr.setEnable(true);
        qr.setUpdatedAt(now);
        qr.setUpdatedBy(adminId);
        return mapper.toResponse(repo.save(qr));
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new LogicException("Файл обязателен");
        }
        long maxBytes = bookingProperties.getMaxPaymentQrSizeMb() * 1024L * 1024L;
        if (file.getSize() > maxBytes) {
            throw new LogicException("Размер QR не должен превышать "
                    + bookingProperties.getMaxPaymentQrSizeMb() + " МБ");
        }
    }
}