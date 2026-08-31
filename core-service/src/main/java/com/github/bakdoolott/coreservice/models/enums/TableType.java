package com.github.bakdoolott.coreservice.models.enums;


public enum TableType {
    CABIN_15(15),
    CABIN_10(10),
    TABLE_2(2),
    TABLE_3(3),
    TABLE_4_CHAIRS(4),
    TABLE_4_SOFA(4),
    BAR(1);

    private final int capacity;

    TableType(int capacity) {
        this.capacity = capacity;
    }
    public int getCapacity(){

        return capacity;
    }
}

