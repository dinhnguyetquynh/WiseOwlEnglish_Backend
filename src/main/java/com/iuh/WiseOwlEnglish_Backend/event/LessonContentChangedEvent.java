package com.iuh.WiseOwlEnglish_Backend.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class LessonContentChangedEvent extends ApplicationEvent {
    private final Long lessonId;

    public LessonContentChangedEvent(Object source, Long lessonId) {
        super(source);
        this.lessonId = lessonId;
    }
}
