package com.diksha.entity;

import com.diksha.enums.StudentClass;
import jakarta.persistence.*;

@Entity
@Table(name = "student_profiles")
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "physics_teacher_id")
    private User physicsTeacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chemistry_teacher_id")
    private User chemistryTeacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maths_teacher_id")
    private User mathsTeacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "biology_teacher_id")
    private User biologyTeacher;

    /** 11 / 12 / Dropper — matches the reference Python backend's StudentProfile.current_class. */
    @Enumerated(EnumType.STRING)
    @Column(name = "current_class")
    private StudentClass currentClass;



    /** JEE or NEET */
    @Column(name = "target_exam")
    private String targetExam;

    public StudentProfile() {
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getPhysicsTeacher() {
        return physicsTeacher;
    }

    public void setPhysicsTeacher(User physicsTeacher) {
        this.physicsTeacher = physicsTeacher;
    }

    public User getChemistryTeacher() {
        return chemistryTeacher;
    }

    public void setChemistryTeacher(User chemistryTeacher) {
        this.chemistryTeacher = chemistryTeacher;
    }

    public User getMathsTeacher() {
        return mathsTeacher;
    }

    public void setMathsTeacher(User mathsTeacher) {
        this.mathsTeacher = mathsTeacher;
    }

    public User getBiologyTeacher() {
        return biologyTeacher;
    }

    public void setBiologyTeacher(User biologyTeacher) {
        this.biologyTeacher = biologyTeacher;
    }

    public StudentClass getCurrentClass() {
        return currentClass;
    }

    public void setCurrentClass(StudentClass currentClass) {
        this.currentClass = currentClass;
    }



    public String getTargetExam() {
        return targetExam;
    }

    public void setTargetExam(String targetExam) {
        this.targetExam = targetExam;
    }
}
