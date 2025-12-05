package com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradeDistribution {
    private String gradeName;
    private Long count; // Lưu ý: Nên để Long (Object) thay vì long (primitive) để tránh lỗi null safe của Hibernate
}
