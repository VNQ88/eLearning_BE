package org.example.elearningbe.chapter.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.elearningbe.common.BaseEntity;
import org.example.elearningbe.course.entities.Course;

@Getter
@Setter
@Entity
public class Chapter extends BaseEntity {
    @Column(length = 100, nullable = false)
    private String title;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "index")
    private Integer index;
}
