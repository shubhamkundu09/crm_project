// EmployeeAuthService.java
package com.crm.service;

import com.crm.dto.ChangePasswordRequest;
import com.crm.dto.EmployeeLoginRequest;
import com.crm.dto.LoginResponse;

public interface EmployeeAuthService {
    LoginResponse authenticateEmployee(EmployeeLoginRequest loginRequest);
    void changePassword(ChangePasswordRequest passwordRequest);
}