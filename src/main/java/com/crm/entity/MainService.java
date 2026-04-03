package com.crm.entity;

public enum MainService {
    LIFE_INSURANCE("Life Insurance"),
    HEALTH_INSURANCE("Health Insurance"),
    GENERAL_INSURANCE("General Insurance"),
    INVESTMENT("Investment"),
    RETIREMENT_PLANNING("Retirement Planning"),
    BUSINESS_INSURANCE("Business Insurance");

    private final String displayName;

    MainService(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}