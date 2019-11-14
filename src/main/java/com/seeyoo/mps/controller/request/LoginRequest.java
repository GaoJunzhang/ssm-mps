package com.seeyoo.mps.controller.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LoginRequest {
    private String account;
    private String password;
    private String code;
}
