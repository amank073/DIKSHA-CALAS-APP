package com.diksha.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "topics")
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String topicName;

    @Column(length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    /*
     * Syllabus class:
     * 11 = Class 11
     * 12 = Class 12
     */
    @Column(nullable = false)
    private String syllabusClass = "11";

    /*
     * Topic Importance Score.
     * SSP algorithm uses this to decide topic priority.
     */
    @Column(nullable = false)
    private double tisScore = 0.0;

    /*
     * Prerequisite topic.
     * Example:
     *
     * Basic Algebra
     *       ↓
     * Quadratic Equation
     *
     * Quadratic Equation ka parent topic = Basic Algebra
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_topic_id")
    private Topic parentTopic;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Topic() {
    }

    public Long getId() {
        return id;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public String getSyllabusClass() {
        return syllabusClass;
    }

    public void setSyllabusClass(String syllabusClass) {
        this.syllabusClass = syllabusClass;
    }

    public double getTisScore() {
        return tisScore;
    }

    public void setTisScore(double tisScore) {
        this.tisScore = tisScore;
    }

    public Topic getParentTopic() {
        return parentTopic;
    }

    public void setParentTopic(Topic parentTopic) {
        this.parentTopic = parentTopic;
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