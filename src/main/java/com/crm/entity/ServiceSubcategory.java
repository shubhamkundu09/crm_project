package com.crm.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public enum ServiceSubcategory {
    // Life Insurance Subcategories
    TERM_LIFE("Term Life Insurance", MainService.LIFE_INSURANCE),
    WHOLE_LIFE("Whole Life Insurance", MainService.LIFE_INSURANCE),
    UNIVERSAL_LIFE("Universal Life Insurance", MainService.LIFE_INSURANCE),
    ENDOWMENT_PLANS("Endowment Plans", MainService.LIFE_INSURANCE),

    // Health Insurance Subcategories
    INDIVIDUAL_HEALTH("Individual Health Insurance", MainService.HEALTH_INSURANCE),
    FAMILY_FLOATER("Family Floater Insurance", MainService.HEALTH_INSURANCE),
    CRITICAL_ILLNESS("Critical Illness Insurance", MainService.HEALTH_INSURANCE),
    SENIOR_CITIZEN("Senior Citizen Health Insurance", MainService.HEALTH_INSURANCE),

    // General Insurance Subcategories
    MOTOR_INSURANCE("Motor Insurance", MainService.GENERAL_INSURANCE),
    HOME_INSURANCE("Home Insurance", MainService.GENERAL_INSURANCE),
    TRAVEL_INSURANCE("Travel Insurance", MainService.GENERAL_INSURANCE),

    // Investment Subcategories
    MUTUAL_FUNDS("Mutual Funds", MainService.INVESTMENT),
    ULIP("ULIP Plans", MainService.INVESTMENT),
    FIXED_DEPOSITS("Fixed Deposits", MainService.INVESTMENT),

    // Retirement Planning Subcategories
    PENSION_PLANS("Pension Plans", MainService.RETIREMENT_PLANNING),
    ANNUITY_PLANS("Annuity Plans", MainService.RETIREMENT_PLANNING),

    // Business Insurance Subcategories
    CORPORATE_HEALTH("Corporate Health Insurance", MainService.BUSINESS_INSURANCE),
    LIABILITY_INSURANCE("Liability Insurance", MainService.BUSINESS_INSURANCE),
    PROPERTY_INSURANCE("Property Insurance", MainService.BUSINESS_INSURANCE);

    private final String displayName;
    private final MainService mainService;
}