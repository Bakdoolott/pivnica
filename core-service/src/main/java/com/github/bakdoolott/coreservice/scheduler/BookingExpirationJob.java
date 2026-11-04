package com.github.bakdoolott.coreservice.scheduler;


import com.github.bakdoolott.coreservice.config.BookingProperties;
import com.github.bakdoolott.coreservice.repositories.BookingRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingExpirationJob {

    private final BookingRepo bookingRepo;
    private final BookingProperties bookingProperties;

    @Scheduled(fixedDelayString = "${booking.expiration-delay-ms:60000}")
    @Transactional
    public void expirePending() {
        LocalDateTime now = LocalDateTime.now(bookingProperties.getClubZone());
        int count = bookingRepo.expirePendingBookings(now);
        if (count > 0) {
            log.info("Expired pending bookings: {}", count);
        }
    }

    @Scheduled(fixedDelayString = "${booking.no-show-delay-ms:60000}")
    @Transactional
    public void markNoShows() {
        LocalDateTime now = LocalDateTime.now(bookingProperties.getClubZone());
        int count = bookingRepo.markNoShows(now);
        if (count > 0) {
            log.info("Marked as NO_SHOW: {}", count);
        }
    }
}