package com.crm.mcsv_auth.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TokenValidationResponse {
    private boolean valid;
    private String errorMessage;

    public TokenValidationResponse() {
    }

    public TokenValidationResponse(boolean valid, String errorMessage) {
        this.valid = valid;
        this.errorMessage = errorMessage;
    }

}
