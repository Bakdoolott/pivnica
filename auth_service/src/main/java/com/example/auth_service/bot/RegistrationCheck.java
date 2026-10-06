package com.example.auth_service.bot;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationCheck {
    private RegistrationEnum check;
    private String phone;
    private String name;

    public RegistrationCheck() {
        this.check = RegistrationEnum.WAITING_NAME;
    }

}
