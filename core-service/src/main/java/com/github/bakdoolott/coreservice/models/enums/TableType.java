package com.github.bakdoolott.coreservice.models.enums;

import java.math.BigDecimal;

public enum TableType {
    CABIN_15(15,new BigDecimal(30000)),
    CABIN_10(10,new BigDecimal(20000)),
    TABLE_2(2, new BigDecimal(4000)),
    TABLE_3(3,new BigDecimal(6000)),
    TABLE_4_CHAIRS(4,new BigDecimal(10000)),
    TABLE_4_SOFA(4,new BigDecimal(15000)),
    BAR(1,BigDecimal.ZERO);

    private final int capacity;
    private final BigDecimal price;

    TableType(int capacity, BigDecimal price) {
        this.capacity = capacity;
        this.price = price;
    }
    public int getCapacity(){
        return capacity;
    }
    public BigDecimal getPrice(){
        return price;
    }
}

