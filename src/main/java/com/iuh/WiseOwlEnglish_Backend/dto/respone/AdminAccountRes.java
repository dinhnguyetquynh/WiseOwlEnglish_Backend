package com.iuh.WiseOwlEnglish_Backend.dto.respone;


import com.iuh.WiseOwlEnglish_Backend.enums.RoleAccount;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class AdminAccountRes {
    private Long id;
    @Enumerated(EnumType.STRING)
    private RoleAccount roleAccount;
}
