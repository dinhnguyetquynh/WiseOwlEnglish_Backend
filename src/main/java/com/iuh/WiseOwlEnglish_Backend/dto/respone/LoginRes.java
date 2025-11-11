package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import com.iuh.WiseOwlEnglish_Backend.enums.RoleAccount;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRes {
    private String accessToken;
    private String refreshToken;
    private boolean hasProfiles;
    private int profileCount;
    private RoleAccount roleAccount;

}
