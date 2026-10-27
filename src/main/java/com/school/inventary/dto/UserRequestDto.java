
package com.school.inventary.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequestDto {


    private String name;
    private String schoolName;
    private String phoneNumber;
    private String email;
    private String password;
    private Integer role;

    private String address;
}
