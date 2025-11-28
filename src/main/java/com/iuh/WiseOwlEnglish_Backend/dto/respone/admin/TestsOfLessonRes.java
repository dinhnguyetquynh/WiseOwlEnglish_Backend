package com.iuh.WiseOwlEnglish_Backend.dto.respone.admin;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.TestResByLesson;
import lombok.Data;

import java.util.List;

@Data
public class TestsOfLessonRes {
    private long id;
    private String unitNumber;
    private String unitName;
    private List<TestAdminByLessonRes> testList;
}
