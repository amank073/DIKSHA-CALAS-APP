package com.diksha.entity;

import com.diksha.enums.PlanStatus;
import com.diksha.enums.PlanVariant;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "study_plans")
public class StudyPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanVariant variant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanStatus status = PlanStatus.ACTIVE;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private double dailyStudyHours;

    @Column(columnDefinition = "TEXT")
    private String phaseBreakdown;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(
            mappedBy = "studyPlan",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<DailySchedule> dailySchedules =
            new ArrayList<>();

    public StudyPlan() {
    }

    public Long getId() {
        return id;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public PlanVariant getVariant() {
        return variant;
    }

    public void setVariant(PlanVariant variant) {
        this.variant = variant;
    }

    public PlanStatus getStatus() {
        return status;
    }

    public void setStatus(PlanStatus status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public double getDailyStudyHours() {
        return dailyStudyHours;
    }

    public void setDailyStudyHours(double dailyStudyHours) {
        this.dailyStudyHours = dailyStudyHours;
    }

    public String getPhaseBreakdown() {
        return phaseBreakdown;
    }

    public void setPhaseBreakdown(String phaseBreakdown) {
        this.phaseBreakdown = phaseBreakdown;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<DailySchedule> getDailySchedules() {
        return dailySchedules;
    }
}
