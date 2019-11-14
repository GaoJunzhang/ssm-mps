package com.seeyoo.mps.controller.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ChangePasswordRequest {
    private String oldPassword;
    private String newPassword;
}
