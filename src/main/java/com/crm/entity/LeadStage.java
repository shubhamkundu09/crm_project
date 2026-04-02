

// LeadStage.java
package com.crm.entity;

public enum LeadStage {
    LEAD_GENERATED("Lead Generated"),
    CONTACTED("Contacted"),
    INTERESTED("Interested"),
    MEETING_BOOKED("Meeting Booked"),
    PROPOSAL_SENT("Proposal Sent"),
    NEGOTIATION("Negotiation"),
    CLOSED("Closed"),
    WHATSAPP_SENT("WhatsApp Sent"),
    CALLS_MADE("Calls Made"),
    FOLLOW_UPS("Follow-ups"),
    MEETINGS_BOOKED("Meetings Booked"),
    MEETINGS_DONE("Meetings Done"),
    CONVERSIONS("Conversions");

    private final String displayName;

    LeadStage(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}