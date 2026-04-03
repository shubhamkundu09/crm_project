package com.crm.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum ServiceSubSubcategory {
    // Term Life Subcategories
    LEVEL_TERM("Level Term Plan", ServiceSubcategory.TERM_LIFE),
    DECREASING_TERM("Decreasing Term Plan", ServiceSubcategory.TERM_LIFE),
    INCREASING_TERM("Increasing Term Plan", ServiceSubcategory.TERM_LIFE),

    // Health Insurance Subcategories
    INDIVIDUAL_COVER("Individual Coverage", ServiceSubcategory.INDIVIDUAL_HEALTH),
    FAMILY_COVER("Family Coverage", ServiceSubcategory.FAMILY_FLOATER),
    TOP_UP_PLANS("Top-up Plans", ServiceSubcategory.FAMILY_FLOATER),

    // Mutual Funds Subcategories
    EQUITY_FUNDS("Equity Funds", ServiceSubcategory.MUTUAL_FUNDS),
    DEBT_FUNDS("Debt Funds", ServiceSubcategory.MUTUAL_FUNDS),
    HYBRID_FUNDS("Hybrid Funds", ServiceSubcategory.MUTUAL_FUNDS),

    // ULIP Subcategories
    EQUITY_ULIP("Equity-oriented ULIP", ServiceSubcategory.ULIP),
    DEBT_ULIP("Debt-oriented ULIP", ServiceSubcategory.ULIP),
    BALANCED_ULIP("Balanced ULIP", ServiceSubcategory.ULIP);

    private final String displayName;
    private final ServiceSubcategory subcategory;
}