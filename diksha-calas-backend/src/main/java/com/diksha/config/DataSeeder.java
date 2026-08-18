package com.diksha.config;

import com.diksha.entity.*;
import com.diksha.enums.RoleType;
import com.diksha.enums.StudentClass;
import com.diksha.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Seeds a fresh database with demo data — ported directly from the
 * reference Python backend's seed_data.py, so the generated study plans
 * are just as detailed (82 JEE topics across Physics/Chemistry/Mathematics
 * with the same TIS scores and prerequisite chains, 3 teachers, 5
 * students, 1 admin, one resource per topic, 18 tests). NEET/Biology has
 * no Python reference to port, so it gets a smaller placeholder set.
 * <p>
 * Runs after RoleSeeder (see @Order) since it depends on Role rows
 * already existing. Only runs on a completely empty `users` table.
 */
@Component
@Order(2)
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;
    private final ResourceRepository resourceRepository;
    private final TestRepository testRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(
            UserRepository userRepository,
            RoleRepository roleRepository,
            TeacherProfileRepository teacherProfileRepository,
            StudentProfileRepository studentProfileRepository,
            SubjectRepository subjectRepository,
            TopicRepository topicRepository,
            ResourceRepository resourceRepository,
            TestRepository testRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.teacherProfileRepository = teacherProfileRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.subjectRepository = subjectRepository;
        this.topicRepository = topicRepository;
        this.resourceRepository = resourceRepository;
        this.testRepository = testRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** name, syllabusClass ("11"/"12"), tisScore, prerequisiteName (or null) — mirrors Python's TopicSeed. */
    private record TopicSeed(String name, String syllabusClass, double tisScore, String prerequisite) {
    }

    private record TeacherSeed(String fullName, String email, String subjectSpecialization) {
    }

    private record StudentSeed(String fullName, String email, StudentClass currentClass, String teacherEmail) {
    }

    // =========================================================
    // TOPICS — ported verbatim from the reference Python backend's
    // seed_data.py TOPICS dict (same names, tis_score, prerequisite chains).
    // =========================================================

    private static final List<TopicSeed> PHYSICS_TOPICS = List.of(
            // --- Class 11 chain ---
            new TopicSeed("Units and Measurements", "11", 32.69, null),
            new TopicSeed("Kinematics: Motion in a Straight Line", "11", 21.35, "Units and Measurements"),
            new TopicSeed("Kinematics: Motion in a Plane", "11", 21.35, "Kinematics: Motion in a Straight Line"),
            new TopicSeed("Laws of Motion", "11", 11.94, "Kinematics: Motion in a Plane"),
            new TopicSeed("Work, Energy and Power", "11", 13.63, "Laws of Motion"),
            new TopicSeed("System of Particles and Rotational Motion", "11", 43.75, "Laws of Motion"),
            new TopicSeed("Gravitation", "11", 22.06, "Laws of Motion"),
            new TopicSeed("Mechanical Properties of Solids", "11", 32.05, "Laws of Motion"),
            new TopicSeed("Mechanical Properties of Fluids", "11", 32.05, "Mechanical Properties of Solids"),
            new TopicSeed("Thermal Properties of Matter", "11", 71.57, "Units and Measurements"),
            new TopicSeed("Thermodynamics", "11", 71.57, "Thermal Properties of Matter"),
            new TopicSeed("Kinetic Theory of Gases", "11", 71.57, "Thermodynamics"),
            new TopicSeed("Oscillations", "11", 18.02, "Work, Energy and Power"),
            new TopicSeed("Waves", "11", 33.14, "Oscillations"),
            // --- Class 12 chain ---
            new TopicSeed("Electric Charges and Fields", "12", 62.59, null),
            new TopicSeed("Electrostatic Potential and Capacitance", "12", 62.59, "Electric Charges and Fields"),
            new TopicSeed("Current Electricity", "12", 49.34, "Electrostatic Potential and Capacitance"),
            new TopicSeed("Moving Charges and Magnetism", "12", 50.61, "Current Electricity"),
            new TopicSeed("Magnetism and Matter", "12", 50.61, "Moving Charges and Magnetism"),
            new TopicSeed("Electromagnetic Induction", "12", 35.46, "Moving Charges and Magnetism"),
            new TopicSeed("Alternating Current", "12", 35.46, "Electromagnetic Induction"),
            new TopicSeed("Electromagnetic Waves", "12", 35.46, "Alternating Current"),
            new TopicSeed("Ray Optics and Optical Instruments", "12", 61.4, "Electromagnetic Waves"),
            new TopicSeed("Wave Optics", "12", 61.4, "Ray Optics and Optical Instruments"),
            new TopicSeed("Dual Nature of Radiation and Matter", "12", 100.0, "Wave Optics"),
            new TopicSeed("Atoms", "12", 100.0, "Dual Nature of Radiation and Matter"),
            new TopicSeed("Nuclei", "12", 100.0, "Atoms"),
            new TopicSeed("Semiconductor Electronics", "12", 100.0, "Nuclei")
    );

    private static final List<TopicSeed> CHEMISTRY_TOPICS = List.of(
            // --- Class 11 chain (Physical + General) ---
            new TopicSeed("Some Basic Concepts of Chemistry (Mole Concept)", "11", 73.24, null),
            new TopicSeed("Structure of Atom", "11", 59.99, "Some Basic Concepts of Chemistry (Mole Concept)"),
            new TopicSeed("Classification of Elements and Periodicity", "11", 28.51, "Structure of Atom"),
            new TopicSeed("Chemical Bonding and Molecular Structure", "11", 65.94, "Structure of Atom"),
            new TopicSeed("States of Matter: Gases and Liquids", "11", 44.42, "Chemical Bonding and Molecular Structure"),
            new TopicSeed("Thermodynamics (Chemistry)", "11", 76.19, "States of Matter: Gases and Liquids"),
            new TopicSeed("Equilibrium", "11", 73.3, "Thermodynamics (Chemistry)"),
            new TopicSeed("Redox Reactions", "11", 73.3, "Some Basic Concepts of Chemistry (Mole Concept)"),
            // --- Class 11 Organic/Inorganic branch ---
            new TopicSeed("Hydrogen", "11", 72.85, "Classification of Elements and Periodicity"),
            new TopicSeed("s-Block Elements", "11", 72.85, "Classification of Elements and Periodicity"),
            new TopicSeed("p-Block Elements (Group 13 & 14)", "11", 44.89, "s-Block Elements"),
            new TopicSeed("Organic Chemistry: Basic Principles and Techniques", "11", 77.8, "Chemical Bonding and Molecular Structure"),
            new TopicSeed("Hydrocarbons", "11", 39.5, "Organic Chemistry: Basic Principles and Techniques"),
            new TopicSeed("Environmental Chemistry", "11", 37.74, "Hydrocarbons"),
            // --- Class 12 chain ---
            new TopicSeed("Solid State", "12", 43.86, null),
            new TopicSeed("Solutions", "12", 59.55, "Solid State"),
            new TopicSeed("Electrochemistry", "12", 63.22, "Solutions"),
            new TopicSeed("Chemical Kinetics", "12", 60.58, "Electrochemistry"),
            new TopicSeed("Surface Chemistry", "12", 44.25, "Chemical Kinetics"),
            new TopicSeed("General Principles of Isolation of Elements", "12", 60.26, "Surface Chemistry"),
            new TopicSeed("p-Block Elements (Group 15-18)", "12", 78.94, "p-Block Elements (Group 13 & 14)"),
            new TopicSeed("d and f Block Elements", "12", 58.9, "p-Block Elements (Group 15-18)"),
            new TopicSeed("Coordination Compounds", "12", 100.0, "d and f Block Elements"),
            new TopicSeed("Haloalkanes and Haloarenes", "12", 50.24, "Hydrocarbons"),
            new TopicSeed("Alcohols, Phenols and Ethers", "12", 34.91, "Haloalkanes and Haloarenes"),
            new TopicSeed("Aldehydes, Ketones and Carboxylic Acids", "12", 38.63, "Alcohols, Phenols and Ethers"),
            new TopicSeed("Amines", "12", 37.49, "Aldehydes, Ketones and Carboxylic Acids"),
            new TopicSeed("Biomolecules", "12", 84.95, "Amines")
    );

    private static final List<TopicSeed> MATHEMATICS_TOPICS = List.of(
            // --- Class 11 chain ---
            new TopicSeed("Sets, Relations and Functions", "11", 33.35, null),
            new TopicSeed("Trigonometric Functions", "11", 30.57, "Sets, Relations and Functions"),
            new TopicSeed("Complex Numbers and Quadratic Equations", "11", 47.52, "Trigonometric Functions"),
            new TopicSeed("Linear Inequalities", "11", 93.08, "Complex Numbers and Quadratic Equations"),
            new TopicSeed("Permutations and Combinations", "11", 42.47, "Sets, Relations and Functions"),
            new TopicSeed("Binomial Theorem", "11", 39.45, "Permutations and Combinations"),
            new TopicSeed("Sequences and Series", "11", 62.74, "Binomial Theorem"),
            new TopicSeed("Straight Lines", "11", 44.25, "Sets, Relations and Functions"),
            new TopicSeed("Conic Sections", "11", 35.45, "Straight Lines"),
            new TopicSeed("Introduction to Three Dimensional Geometry", "11", 75.24, "Conic Sections"),
            new TopicSeed("Limits and Derivatives", "11", 100.0, "Trigonometric Functions"),
            new TopicSeed("Mathematical Reasoning", "11", 93.08, "Sets, Relations and Functions"),
            new TopicSeed("Statistics", "11", 93.08, "Sequences and Series"),
            new TopicSeed("Probability", "11", 54.2, "Permutations and Combinations"),
            // --- Class 12 chain ---
            new TopicSeed("Relations and Functions (Advanced)", "12", 33.35, null),
            new TopicSeed("Inverse Trigonometric Functions", "12", 22.09, "Relations and Functions (Advanced)"),
            new TopicSeed("Matrices", "12", 85.28, "Relations and Functions (Advanced)"),
            new TopicSeed("Determinants", "12", 85.28, "Matrices"),
            new TopicSeed("Continuity and Differentiability", "12", 100.0, "Inverse Trigonometric Functions"),
            new TopicSeed("Applications of Derivatives", "12", 68.05, "Continuity and Differentiability"),
            new TopicSeed("Integrals", "12", 34.76, "Continuity and Differentiability"),
            new TopicSeed("Applications of Integrals", "12", 63.53, "Integrals"),
            new TopicSeed("Differential Equations", "12", 51.18, "Applications of Integrals"),
            new TopicSeed("Vector Algebra", "12", 47.42, "Determinants"),
            new TopicSeed("Three Dimensional Geometry (Advanced)", "12", 75.24, "Vector Algebra"),
            new TopicSeed("Linear Programming", "12", 93.08, "Three Dimensional Geometry (Advanced)"),
            new TopicSeed("Probability (Advanced)", "12", 54.2, "Probability")
    );

    // NEET has no Python reference data — Physics/Chemistry are shared with
    // JEE conceptually, so NEET reuses the SAME two lists above; Biology
    // gets a smaller placeholder set since there's nothing to port.
    private static final List<TopicSeed> BIOLOGY_TOPICS = List.of(
            new TopicSeed("Diversity in Living World", "11", 40, null),
            new TopicSeed("Structural Organisation in Animals and Plants", "11", 45, "Diversity in Living World"),
            new TopicSeed("Cell Structure and Function", "11", 78, "Structural Organisation in Animals and Plants"),
            new TopicSeed("Plant Physiology", "11", 60, "Cell Structure and Function"),
            new TopicSeed("Human Physiology", "11", 82, "Cell Structure and Function"),
            new TopicSeed("Reproduction", "12", 75, null),
            new TopicSeed("Genetics and Evolution", "12", 85, "Reproduction"),
            new TopicSeed("Biology and Human Welfare", "12", 45, "Genetics and Evolution"),
            new TopicSeed("Biotechnology and Its Applications", "12", 65, "Genetics and Evolution"),
            new TopicSeed("Ecology and Environment", "12", 55, "Biology and Human Welfare")
    );

    private static final List<TeacherSeed> TEACHERS = List.of(
    );

    private static final List<StudentSeed> STUDENTS = List.of(
    );

    private static final int TOPIC_WISE_TESTS_PER_TEACHER = 4;
    private static final int SUBJECT_WISE_TESTS_PER_TEACHER = 2;

    private static final String DEFAULT_PASSWORD = "Pass@12345";

    @Override
    public void run(String... args) {

        if (userRepository.count() > 0) {
            System.out.println("[DataSeeder] Users already exist — skipping seed.");
            return;
        }

        System.out.println("[DataSeeder] Seeding demo data (ported from Python reference)...");

        Role adminRole = roleRepository.findByName(RoleType.ADMIN)
                .orElseThrow(() -> new RuntimeException("ADMIN role not found — did RoleSeeder run?"));
        Role teacherRole = roleRepository.findByName(RoleType.TEACHER)
                .orElseThrow(() -> new RuntimeException("TEACHER role not found"));
        Role studentRole = roleRepository.findByName(RoleType.STUDENT)
                .orElseThrow(() -> new RuntimeException("STUDENT role not found"));

        // ---- Admin ----
        createUser("System", "Admin", "admin90@gmail.com", "Admin@123", adminRole);


        // ---- Teachers (with TeacherProfile + specialization) ----
        Map<String, User> teachersByEmail = new HashMap<>();
        for (TeacherSeed t : TEACHERS) {
            User teacher = createUser(firstName(t.fullName()), lastName(t.fullName()), t.email(), DEFAULT_PASSWORD, teacherRole);
            createTeacherProfile(teacher, t.subjectSpecialization());
            teachersByEmail.put(t.email(), teacher);
        }

        // ---- Subjects + full Python-ported topic set ----
        Subject jeePhysics = createSubject("Physics (JEE)");
        Subject jeeChemistry = createSubject("Chemistry (JEE)");
        Subject jeeMaths = createSubject("Mathematics (JEE)");

        seedTopicsAndResources(jeePhysics, PHYSICS_TOPICS);
        seedTopicsAndResources(jeeChemistry, CHEMISTRY_TOPICS);
        seedTopicsAndResources(jeeMaths, MATHEMATICS_TOPICS);

        // ---- Subjects (Physics/Chemistry reused, Biology new) ----
        Subject neetPhysics = createSubject("Physics (NEET)");
        Subject neetChemistry = createSubject("Chemistry (NEET)");
        Subject neetBiology = createSubject("Biology (NEET)");

        seedTopicsAndResources(neetPhysics, PHYSICS_TOPICS);
        seedTopicsAndResources(neetChemistry, CHEMISTRY_TOPICS);
        seedTopicsAndResources(neetBiology, BIOLOGY_TOPICS);

        // ---- Tests: Skip if no teachers ----
        if (!teachersByEmail.isEmpty()) {
            User physicsTeacher = teachersByEmail.get("anjali.rao@diksha-calas.edu");
            User chemistryTeacher = teachersByEmail.get("sunil.verma@diksha-calas.edu");
            User mathsTeacher = teachersByEmail.get("priya.nair@diksha-calas.edu");

            if (physicsTeacher != null) seedTests(physicsTeacher, jeePhysics);
            if (chemistryTeacher != null) seedTests(chemistryTeacher, jeeChemistry);
            if (mathsTeacher != null) seedTests(mathsTeacher, jeeMaths);
        }

        // ---- Students (with StudentProfile + currentClass + cohortId + teacher) ----
        for (StudentSeed s : STUDENTS) {
            User student = createUser(firstName(s.fullName()), lastName(s.fullName()), s.email(), DEFAULT_PASSWORD, studentRole);
            User assignedTeacher = teachersByEmail.get(s.teacherEmail());
            createStudentProfile(student, s.currentClass(), assignedTeacher);
        }

        System.out.println("[DataSeeder] Done — seeded " +
                (PHYSICS_TOPICS.size() + CHEMISTRY_TOPICS.size() + MATHEMATICS_TOPICS.size()) +
                " JEE topics + " + BIOLOGY_TOPICS.size() + " NEET Biology topics. " +
                "Login with admin90@gmail.com / Admin@123 (change this immediately in production).");
    }

    private String firstName(String fullName) {
        int idx = fullName.indexOf(' ');
        return idx == -1 ? fullName : fullName.substring(0, idx);
    }

    private String lastName(String fullName) {
        int idx = fullName.indexOf(' ');
        return idx == -1 ? "" : fullName.substring(idx + 1);
    }

    private User createUser(String firstName, String lastName, String email, String rawPassword, Role role) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setPhone("9999999999");
        user.setRole(role);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    private void createTeacherProfile(User teacher, String specialization) {
        TeacherProfile profile = new TeacherProfile();
        profile.setUser(teacher);
        profile.setSubjectSpecialization(specialization);
        teacherProfileRepository.save(profile);
    }

    private void createStudentProfile(User student, StudentClass studentClass, User teacher) {
        StudentProfile profile = new StudentProfile();
        profile.setUser(student);
        profile.setCurrentClass(studentClass);
        studentProfileRepository.save(profile);
    }

    private Subject createSubject(String name) {
        Subject subject = new Subject();
        subject.setSubjectName(name);
        subject.setActive(true);
        return subjectRepository.save(subject);
    }

    /**
     * Two-pass insertion, matching Python's loader description: insert
     * every topic first (parent left null), then resolve each
     * `prerequisite` name to the sibling Topic just inserted and set it
     * as parentTopic — this avoids relying on prerequisite names always
     * appearing earlier in the list.
     */
    private void seedTopicsAndResources(Subject subject, List<TopicSeed> seeds) {
        Map<String, Topic> savedByName = new HashMap<>();

        for (TopicSeed seed : seeds) {
            Topic topic = new Topic();
            topic.setTopicName(seed.name());
            topic.setSubject(subject);
            topic.setSyllabusClass(seed.syllabusClass());
            topic.setTisScore(seed.tisScore());
            topic.setActive(true);
            savedByName.put(seed.name(), topicRepository.save(topic));
        }

        for (TopicSeed seed : seeds) {
            if (seed.prerequisite() == null) continue;
            Topic parent = savedByName.get(seed.prerequisite());
            if (parent == null) continue;
            Topic child = savedByName.get(seed.name());
            child.setParentTopic(parent);
            topicRepository.save(child);
        }

        for (Topic topic : savedByName.values()) {
            Resource resource = new Resource();
            resource.setTitle(topic.getTopicName() + " — NCERT-aligned Video Lecture");
            resource.setResourceType("Video");
            resource.setResourceUrl("https://content.dikshacalas.edu/videos/"
                    + topic.getTopicName().toLowerCase().replace(" ", "-").replace(",", ""));
            resource.setTopic(topic);
            resource.setActive(true);
            resourceRepository.save(resource);
        }
    }

    private void seedTests(User teacher, Subject subject) {
        List<Topic> topics = topicRepository.findBySubjectId(subject.getId());
        if (topics.isEmpty()) return;

        for (int i = 0; i < TOPIC_WISE_TESTS_PER_TEACHER && i < topics.size(); i++) {
            Topic topic = topics.get(i);

            Test test = new Test();
            test.setTitle(subject.getSubjectName() + " — " + topic.getTopicName() + " Topic Test");
            test.setDescription("Auto-seeded topic wise test");
            test.setTestType("Topic Wise");
            test.setDurationMinutes(30);
            test.setTotalMarks(50);
            test.setLink("https://tests.dikshacalas.edu/" + topic.getTopicName().toLowerCase().replace(" ", "-").replace(",", ""));
            test.setMixedSubject(false);
            test.setTopic(topic);
            test.setCreatedByTeacher(teacher);
            test.setActive(true);
            testRepository.save(test);
        }

        for (int i = 0; i < SUBJECT_WISE_TESTS_PER_TEACHER; i++) {
            Test test = new Test();
            test.setTitle(subject.getSubjectName() + " — Subject Wise Test " + (i + 1));
            test.setDescription("Auto-seeded subject wise (mixed topic) test");
            test.setTestType("Subject Wise");
            test.setDurationMinutes(60);
            test.setTotalMarks(100);
            test.setLink("https://tests.dikshacalas.edu/" + subject.getSubjectName().toLowerCase() + "-full-" + (i + 1));
            test.setMixedSubject(true);
            test.setTopic(null);
            test.setCreatedByTeacher(teacher);
            test.setActive(true);
            testRepository.save(test);
        }
    }
}
