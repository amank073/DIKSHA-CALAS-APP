package com.diksha.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tests")
public class Test {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private String testType;

    @Column(nullable = false)
    private int durationMinutes;

    @Column(nullable = false)
    private int totalMarks;

    /** Actual test URL (Google Form, external test platform link, etc.). */
    @Column(length = 2000)
    private String link;

    /*
     * false = Topic Wise Test
     * true  = Subject Wise / mixed-subject Test
     */
    @Column(nullable = false)
    private boolean mixedSubject = false;

    /**
     * Nullable: a Subject Wise / mixed-subject test doesn't belong to one
     * specific topic (matches the reference Python backend's Test.topic_id
     * being optional for the same reason).
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "topic_id", nullable = true)
    private Topic topic;

    /** The teacher who authored this test — drives AssessmentScheduler's
     *  student-assigned-teacher-only test pool (see StudyPlanServiceImpl). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_teacher_id", nullable = false)
    private User createdByTeacher;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Test() {
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTestType() {
        return testType;
    }

    public void setTestType(String testType) {
        this.testType = testType;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public int getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(int totalMarks) {
        this.totalMarks = totalMarks;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public boolean isMixedSubject() {
        return mixedSubject;
    }

    public void setMixedSubject(boolean mixedSubject) {
        this.mixedSubject = mixedSubject;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public User getCreatedByTeacher() {
        return createdByTeacher;
    }

    public void setCreatedByTeacher(User createdByTeacher) {
        this.createdByTeacher = createdByTeacher;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
