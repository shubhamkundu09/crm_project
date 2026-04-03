package com.crm.dto;

import lombok.Data;

@Data
public class EmployeeBasicDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
}