package com.github.bakdoolott.coreservice.services.impl;

import com.github.bakdoolott.coreservice.exceptions.ConflictException;
import com.github.bakdoolott.coreservice.exceptions.LogicException;
import com.github.bakdoolott.coreservice.exceptions.NotFoundException;
import com.github.bakdoolott.coreservice.mappers.BookingPriceMapper;
import com.github.bakdoolott.coreservice.models.BookingPrice;
import com.github.bakdoolott.coreservice.models.dto.PriceCreateDto;
import com.github.bakdoolott.coreservice.models.dto.PriceUpdateDto;
import com.github.bakdoolott.coreservice.models.dto.response.PriceResponse;
import com.github.bakdoolott.coreservice.repositories.BookingPriceRepo;
import com.github.bakdoolott.coreservice.services.BookingPriceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class BookingPriceServiceImpl implements BookingPriceService {

    private final BookingPriceRepo priceRepo;
    private final BookingPriceMapper priceMapper;

    public BookingPriceServiceImpl(BookingPriceRepo priceRepo, BookingPriceMapper priceMapper) {
        this.priceRepo = priceRepo;
        this.priceMapper = priceMapper;
    }

    @Override
    @Transactional
    public PriceResponse createPrice(PriceCreateDto dto) {
        if(priceRepo.findByEndAtIsNullAndEnableTrue().isPresent()){
            throw new ConflictException("Активная цена уже существует");
        }
        LocalDateTime now = LocalDateTime.now();

        BookingPrice price = new BookingPrice();
        price.setPrice(dto.getPrice());
        price.setCreatedAt(now);
        price.setStartAt(now);
        price.setEndAt(null);
        price.setEnable(true);

        return priceMapper.toResponse(priceRepo.save(price));
    }

    @Override
    @Transactional
    public PriceResponse updatePrice(PriceUpdateDto dto) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime newStartAt = dto.getEffectiveFrom();
        if (newStartAt == null){
            newStartAt = now;
        }
        if(newStartAt.isBefore(now)){
            throw new LogicException("Дата начала не может быть в прошлом");
        }
        BookingPrice current = priceRepo.findByEndAtIsNullAndEnableTrue()
                .orElseThrow(() -> new NotFoundException("Активная цена не найдена"));

        if(!newStartAt.isAfter(current.getStartAt())){
            throw new LogicException("Новая цена должна начинаться позже текущей");
        }
        current.setEndAt(newStartAt);
        priceRepo.save(current);

        BookingPrice newPrice = new BookingPrice();
        newPrice.setPrice(dto.getPrice());
        newPrice.setCreatedAt(now);
        newPrice.setStartAt(newStartAt);
        newPrice.setEndAt(null);
        newPrice.setEnable(true);

        return priceMapper.toResponse(priceRepo.save(newPrice));
    }

    @Override
    public BookingPrice getCurrentPriceEntity() {
        return getPriceAt(LocalDateTime.now());
    }

    @Override
    public BookingPrice getPriceAt(LocalDateTime dateTime) {
        return priceRepo.findActiveAt(dateTime)
                .orElseThrow(() -> new NotFoundException("Цена бронирования не установлена"));
    }

    @Override
    public BigDecimal getCurrentPrice() {
        return getCurrentPriceEntity().getPrice();
    }
}
