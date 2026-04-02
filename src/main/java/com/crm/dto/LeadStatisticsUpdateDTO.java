
// LeadStatisticsUpdateDTO.java
package com.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadStatisticsUpdateDTO {
    private Boolean whatsappSent;
    private Boolean callsMade;
    private Boolean followUp;
    private Boolean meetingBooked;
    private Boolean meetingDone;
}