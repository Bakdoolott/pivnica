package com.github.bakdoolott.coreservice.services.impl;

import com.github.bakdoolott.coreservice.config.BookingProperties;
import com.github.bakdoolott.coreservice.exceptions.ConflictException;
import com.github.bakdoolott.coreservice.exceptions.LogicException;
import com.github.bakdoolott.coreservice.exceptions.NotFoundException;
import com.github.bakdoolott.coreservice.mappers.BookingPriceMapper;
import com.github.bakdoolott.coreservice.models.BookingPrice;
import com.github.bakdoolott.coreservice.models.dto.PriceCreateDto;
import com.github.bakdoolott.coreservice.models.dto.PriceUpdateDto;
import com.github.bakdoolott.coreservice.models.dto.response.PriceResponse;
import com.github.bakdoolott.coreservice.models.enums.TableType;
import com.github.bakdoolott.coreservice.repositories.BookingPriceRepo;
import com.github.bakdoolott.coreservice.services.BookingPriceService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class BookingPriceServiceImpl implements BookingPriceService {

    private final BookingPriceRepo priceRepo;
    private final BookingPriceMapper priceMapper;
    private final BookingProperties bookingProperties;

    public BookingPriceServiceImpl(BookingPriceRepo priceRepo, BookingPriceMapper priceMapper, BookingProperties bookingProperties) {
        this.priceRepo = priceRepo;
        this.priceMapper = priceMapper;
        this.bookingProperties = bookingProperties;
    }
    private LocalDateTime now(){
        return LocalDateTime.now(bookingProperties.getClubZone());
    }
    private BigDecimal money(BigDecimal amount){
        return amount.setScale(2, RoundingMode.HALF_UP);
    }


    @Override
    @Transactional
    public PriceResponse createPrice(PriceCreateDto dto) {
        LocalDateTime now = now();
        LocalDateTime startAt = Objects.requireNonNullElse(dto.getEffectiveFrom(), now);
        if(startAt.isBefore(now)){
            throw new LogicException("Дата начала не может быть в прошлом");
        }
        if(priceRepo.findOpenForUpdate(dto.getTableType()).isPresent()){
            throw new ConflictException(
                    "Активная цена для "+ dto.getTableType() + " уже существует");
        }

        BookingPrice price = new BookingPrice();
        price.setTableType(dto.getTableType());
        price.setPrice(money(dto.getPrice()));
        price.setCreatedAt(now);
        price.setStartAt(startAt);
        price.setEndAt(null);
        price.setEnable(true);

        return priceMapper.toResponse(save(price));
    }

    @Override
    @Transactional
    public PriceResponse updatePrice(PriceUpdateDto dto) {
        LocalDateTime now = now();
        LocalDateTime newStartAt = Objects.requireNonNullElse(dto.getEffectiveFrom(),now);

        if(newStartAt.isBefore(now)){
            throw new LogicException("Дата начала не может быть в прошлом");
        }

        BookingPrice current = priceRepo.findOpenForUpdate(dto.getTableType())
                .orElseThrow(() -> new NotFoundException(
                        "Активная цена для " + dto.getTableType() + " не найдена"));

        if(newStartAt.isBefore(current.getStartAt())) {
            throw new LogicException("Новая цена должна начинаться позже текущей");
        }
        BigDecimal newPriceValue = money(dto.getPrice());
        if(current.getPrice().compareTo(newPriceValue) == 0) {
            throw new ConflictException("Цена не изменилась");
        }

        current.setEndAt(newStartAt);
        priceRepo.saveAndFlush(current);

        BookingPrice next = new BookingPrice();
        next.setTableType(dto.getTableType());
        next.setPrice(newPriceValue);
        next.setCreatedAt(newStartAt);
        next.setEndAt(null);
        next.setEnable(true);

        return priceMapper.toResponse(save(next));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<TableType, BookingPrice> getActivePricesAt(LocalDateTime at) {
        Map<TableType, BookingPrice> byType = new EnumMap<>(TableType.class);
        for (BookingPrice price : priceRepo.findAllActiveAt(at)) {
            BookingPrice existing = byType.get(price.getTableType());
            if (existing == null || price.getStartAt().isAfter(existing.getStartAt())) {
                byType.put(price.getTableType(), price);
            }
        }
        return byType;
    }

    @Override
    @Transactional(readOnly = true)
    public BookingPrice getPriceEntityAt(TableType tableType, LocalDateTime at) {
        return priceRepo.findActiveAt(tableType,at).
                orElseThrow(() -> new NotFoundException(
                        "Цена для " + tableType + " не установлена"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PriceResponse> getCurrentPrices() {
        return getActivePricesAt(now()).values().stream()
                .map(priceMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PriceResponse getCurrentPrice(TableType tableType) {
        return priceMapper.toResponse(getPriceEntityAt(tableType,now()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PriceResponse> getHistory(TableType tableType) {
        return priceRepo.findHistory(tableType).stream()
                .map(priceMapper::toResponse)
                .toList();
    }

    private BookingPrice save(BookingPrice price) {
        try {
            return priceRepo.saveAndFlush(price);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException(
                    "Цена для " + price.getTableType() + " изменена другим администратором, повторите");
        }
    }
}