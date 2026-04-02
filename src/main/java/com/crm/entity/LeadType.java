// LeadType.java
package com.crm.entity;

public enum LeadType {
    HOT("Hot - Ready to buy"),
    WARM("Warm - Interested"),
    COLD("Cold - Just contacted");

    private final String description;

    LeadType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}