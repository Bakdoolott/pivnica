package com.github.bakdoolott.coreservice.controller;

import com.github.bakdoolott.coreservice.models.dto.BookingCreateDto;
import com.github.bakdoolott.coreservice.models.dto.response.BookingResponse;
import com.github.bakdoolott.coreservice.security.AuthenticatedUserResolver;
import com.github.bakdoolott.coreservice.services.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/core/booking")
@Tag(name = "Бронирования")
public class BookingController {

    private final BookingService bookingService;
    private final AuthenticatedUserResolver userResolver;

    public BookingController(BookingService bookingService, AuthenticatedUserResolver userResolver) {
        this.bookingService = bookingService;
        this.userResolver = userResolver;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cоздание брони")
    public ResponseEntity<BookingResponse> create(Authentication authentication,
                                                  @Valid @RequestBody BookingCreateDto request){
        Long userId = userResolver.getUserId(authentication);
        return new ResponseEntity<>(bookingService.createBooking(userId,request), HttpStatus.CREATED);
    }
}
