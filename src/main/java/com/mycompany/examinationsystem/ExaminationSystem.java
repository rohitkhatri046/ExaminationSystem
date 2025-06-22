package com.mycompany.examinationsystem;

import java.io.*;
import java.util.*;
import java.time.*;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

// Base User class
abstract class User implements Serializable {
    protected String userId;
    protected String password;
    protected String name;
    
    public User(String userId, String password, String name) {
        this.userId = userId;
        this.password = password;
        this.name = name;
    }
    
    public boolean authenticate(String userId, String password) {
        return this.userId.equals(userId) && this.password.equals(password);
    }
    
    public String getUserId() { return userId; }
    public String getName() { return name; }
    
    abstract void showMenu(ExaminationSystem system);
}

// Teacher class
class Teacher extends User {
    public Teacher(String userId, String password, String name) {
        super(userId, password, name);
    }
    
    @Override
    void showMenu(ExaminationSystem system) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\nTeacher Menu:");
            System.out.println("1. Create Question Bank");
            System.out.println("2. Create Quiz");
            System.out.println("3. View Quiz Results");
            System.out.println("4. View Analytics");
            System.out.println("5. View Attendance");
            System.out.println("6. Logout");
            
            System.out.print("Enter choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline
            
            switch (choice) {
                case 1 -> system.createQuestionBank(this);
                case 2 -> system.createQuiz(this);
                case 3 -> system.viewQuizResults(this);
                case 4 -> system.viewAnalytics(this);
                case 5 -> system.viewAttendance(this);
                case 6 -> {
                    return;
                }
                default -> System.out.println("Invalid choice!");
            }
        }
    }
}

// Student class
class Student extends User {
    public Student(String userId, String password, String name) {
        super(userId, password, name);
    }
    
    @Override
    void showMenu(ExaminationSystem system) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\nStudent Menu:");
            System.out.println("1. Attempt Quiz");
            System.out.println("2. View Results");
            System.out.println("3. Logout");
            
            System.out.print("Enter choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline
            
            switch (choice) {
                case 1 -> system.attemptQuiz(this);
                case 2 -> system.viewStudentResults(this);
                case 3 -> {
                    return;
                }
                default -> System.out.println("Invalid choice!");
            }
        }
    }
}

// Question hierarchy
abstract class Question implements Serializable {
    protected String questionId;
    protected String topic;
    protected String questionText;
    protected int marks;
    
    public Question(String questionId, String topic, String questionText, int marks) {
        this.questionId = questionId;
        this.topic = topic;
        this.questionText = questionText;
        this.marks = marks;
    }
    
    public String getQuestionId() { return questionId; }
    public String getTopic() { return topic; }
    public String getQuestionText() { return questionText; }
    public int getMarks() { return marks; }
    
    abstract void displayQuestion();
    abstract boolean checkAnswer(String answer);
}

class MCQQuestion extends Question {
    private final List<String> options;
    private int correctOption;
    
    public MCQQuestion(String questionId, String topic, String questionText, int marks, 
                      List<String> options, int correctOption) {
        super(questionId, topic, questionText, marks);
        this.options = options;
        this.correctOption = correctOption;
    }
    
    @Override
    void displayQuestion() {
        System.out.println(questionText);
        for (int i = 0; i < options.size(); i++) {
            System.out.println((i+1) + ". " + options.get(i));
        }
    }
    
    @Override
    boolean checkAnswer(String answer) {
        try {
            int selectedOption = Integer.parseInt(answer);
            return selectedOption == correctOption + 1;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    // For randomization
    public void shuffleOptions() {
        Collections.shuffle(options);
        // Update correct option index after shuffling
        String correctAnswer = options.get(correctOption);
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).equals(correctAnswer)) {
                correctOption = i;
                break;
            }
        }
    }
}

class TrueFalseQuestion extends Question {
    private final boolean correctAnswer;
    
    public TrueFalseQuestion(String questionId, String topic, String questionText, int marks, 
                            boolean correctAnswer) {
        super(questionId, topic, questionText, marks);
        this.correctAnswer = correctAnswer;
    }
    
    @Override
    void displayQuestion() {
        System.out.println(questionText);
        System.out.println("1. True");
        System.out.println("2. False");
    }
    
    @Override
    boolean checkAnswer(String answer) {
        try {
            int selectedOption = Integer.parseInt(answer);
            return (selectedOption == 1 && correctAnswer) || 
                   (selectedOption == 2 && !correctAnswer);
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

class SubjectiveQuestion extends Question {
    private final String modelAnswer;
    
    public SubjectiveQuestion(String questionId, String topic, String questionText, int marks, 
                             String modelAnswer) {
        super(questionId, topic, questionText, marks);
        this.modelAnswer = modelAnswer;
    }
    
    @Override
    void displayQuestion() {
        System.out.println(questionText);
    }
    
    @Override
    boolean checkAnswer(String answer) {
        // For subjective questions, we'll just return true for now
        // In a real system, this would require manual grading
        return true;
    }
}

// Quiz class
class Quiz implements Serializable {
    private final String quizId;
    private final String courseId;
    private final Teacher createdBy;
    private final LocalDateTime startTime;
    private final Duration duration;
    private final List<Question> questions;
    private final Map<String, QuizAttempt> attempts;
    
    public Quiz(String quizId, String courseId, Teacher createdBy, LocalDateTime startTime, 
               Duration duration, List<Question> questions) {
        this.quizId = quizId;
        this.courseId = courseId;
        this.createdBy = createdBy;
        this.startTime = startTime;
        this.duration = duration;
        this.questions = questions;
        this.attempts = new HashMap<>();
    }
    
    public String getQuizId() { return quizId; }
    public String getCourseId() { return courseId; }
    public Teacher getCreatedBy() { return createdBy; }
    public LocalDateTime getStartTime() { return startTime; }
    public Duration getDuration() { return duration; }
    public List<Question> getQuestions() { return questions; }
    
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startTime) && now.isBefore(startTime.plus(duration));
    }
    
    public void shuffleQuestions() {
        Collections.shuffle(questions);
    }
    
    public QuizAttempt attemptQuiz(Student student) {
        if (!isActive()) {
            System.out.println("Quiz is not currently active!");
            return null;
        }
        
        if (attempts.containsKey(student.getUserId())) {
            System.out.println("You have already attempted this quiz!");
            return null;
        }
        
        QuizAttempt attempt = new QuizAttempt(student, this);
        attempts.put(student.getUserId(), attempt);
        return attempt;
    }
    
    public QuizAttempt getAttempt(String studentId) {
        return attempts.get(studentId);
    }
    
    public Map<String, QuizAttempt> getAttempts() {
        return attempts;
    }
}

// Quiz Attempt class
class QuizAttempt implements Serializable {
    private final Student student;
    private final Quiz quiz;
    final Map<String, String> answers;
    private int score;
    private boolean graded;
    
    public QuizAttempt(Student student, Quiz quiz) {
        this.student = student;
        this.quiz = quiz;
        this.answers = new HashMap<>();
        this.score = 0;
        this.graded = false;
    }
    
    public void recordAnswer(String questionId, String answer) {
        answers.put(questionId, answer);
    }
    
    public void gradeQuiz() {
        if (graded) return;
        
        score = 0;
        for (Question question : quiz.getQuestions()) {
            String answer = answers.get(question.getQuestionId());
            if (answer != null && question.checkAnswer(answer)) {
                score += question.getMarks();
            }
        }
        graded = true;
    }
    
    public int getScore() {
        if (!graded) gradeQuiz();
        return score;
    }
    
    public Student getStudent() { return student; }
    public Quiz getQuiz() { return quiz; }
    public boolean isGraded() { return graded; }
}

// Course class
class Course implements Serializable {
    private final String courseId;
    private final String courseName;
    private final Teacher instructor;
    private final List<Student> enrolledStudents;
    private final List<Quiz> quizzes;
    
    public Course(String courseId, String courseName, Teacher instructor) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.instructor = instructor;
        this.enrolledStudents = new ArrayList<>();
        this.quizzes = new ArrayList<>();
    }
    
    public String getCourseId() { return courseId; }
    public String getCourseName() { return courseName; }
    public Teacher getInstructor() { return instructor; }
    public List<Student> getEnrolledStudents() { return enrolledStudents; }
    public List<Quiz> getQuizzes() { return quizzes; }
    
    public void enrollStudent(Student student) {
        if (!enrolledStudents.contains(student)) {
            enrolledStudents.add(student);
        }
    }
    
    public void addQuiz(Quiz quiz) {
        quizzes.add(quiz);
    }
    
    public Quiz getQuiz(String quizId) {
        for (Quiz quiz : quizzes) {
            if (quiz.getQuizId().equals(quizId)) {
                return quiz;
            }
        }
        return null;
    }
}

// Main Examination System
class ExaminationSystem {
    private Map<String, User> users;
    private Map<String, Course> courses;
    private Map<String, List<Question>> questionBanks;
    private User currentUser;
    
    public ExaminationSystem() {
        this.users = new HashMap<>();
        this.courses = new HashMap<>();
        this.questionBanks = new HashMap<>();
        this.currentUser = null;
        
        // Initialize with some sample data
        initializeSampleData();
    }
    
    private void initializeSampleData() {
        // Create sample teacher
        Teacher teacher = new Teacher("t1", "pass", "Dr. Smith");
        users.put(teacher.getUserId(), teacher);
        
        // Create sample students
        Student student1 = new Student("s1", "pass", "Ali Khan");
        Student student2 = new Student("s2", "pass", "Sara Ahmed");
        users.put(student1.getUserId(), student1);
        users.put(student2.getUserId(), student2);
        
        // Create sample course
        Course course = new Course("OOPT-2002", "Object Oriented Programming Theory", teacher);
        course.enrollStudent(student1);
        course.enrollStudent(student2);
        courses.put(course.getCourseId(), course);
        
        // Create sample questions
        List<Question> questions = new ArrayList<>();
        questions.add(new MCQQuestion("q1", "Inheritance", 
            "Which keyword is used for inheritance in Java?", 5, 
            Arrays.asList("extends", "implements", "inherits", "derives"), 0));
        questions.add(new TrueFalseQuestion("q2", "Polymorphism", 
            "Method overloading is an example of runtime polymorphism.", 3, false));
        questionBanks.put(course.getCourseId(), questions);
    }
    
    public void login() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\nLogin");
        System.out.print("User ID: ");
        String userId = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        
        User user = users.get(userId);
        if (user != null && user.authenticate(userId, password)) {
            currentUser = user;
            System.out.println("Login successful! Welcome " + user.getName());
            user.showMenu(this);
        } else {
            System.out.println("Invalid credentials!");
        }
    }
    
    public void logout() {
        currentUser = null;
        System.out.println("Logged out successfully!");
    }
    
    public void createQuestionBank(Teacher teacher) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\nCreate Question Bank");
        
        System.out.println("Available Courses:");
        for (Course course : courses.values()) {
            if (course.getInstructor().equals(teacher)) {
                System.out.println(course.getCourseId() + " - " + course.getCourseName());
            }
        }
        
        System.out.print("Enter Course ID: ");
        String courseId = scanner.nextLine();
        
        Course course = courses.get(courseId);
        if (course == null || !course.getInstructor().equals(teacher)) {
            System.out.println("Invalid course selection!");
            return;
        }
        
        List<Question> questions = questionBanks.getOrDefault(courseId, new ArrayList<>());
        
        while (true) {
            System.out.println("\nQuestion Bank for " + course.getCourseName());
            System.out.println("1. Add MCQ Question");
            System.out.println("2. Add True/False Question");
            System.out.println("3. Add Subjective Question");
            System.out.println("4. View Questions");
            System.out.println("5. Finish");
            
            System.out.print("Enter choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline
            
            switch (choice) {
                case 1 -> addMCQQuestion(scanner, questions);
                case 2 -> addTrueFalseQuestion(scanner, questions);
                case 3 -> addSubjectiveQuestion(scanner, questions);
                case 4 -> viewQuestions(questions);
                case 5 -> {
                    questionBanks.put(courseId, questions);
                    System.out.println("Question bank saved successfully!");
                    return;
                }
                default -> System.out.println("Invalid choice!");
            }
        }
    }
    
    private void addMCQQuestion(Scanner scanner, List<Question> questions) {
        System.out.println("\nAdd MCQ Question");
        System.out.print("Question ID: ");
        String questionId = scanner.nextLine();
        System.out.print("Topic: ");
        String topic = scanner.nextLine();
        System.out.print("Question Text: ");
        String questionText = scanner.nextLine();
        System.out.print("Marks: ");
        int marks = scanner.nextInt();
        scanner.nextLine(); // consume newline
        
        List<String> options = new ArrayList<>();
        System.out.println("Enter options (enter empty line to finish):");
        String option;
        int optionNum = 1;
        while (true) {
            System.out.print("Option " + optionNum + ": ");
            option = scanner.nextLine();
            if (option.isEmpty()) break;
            options.add(option);
            optionNum++;
        }
        
        System.out.print("Correct option number (1-" + options.size() + "): ");
        int correctOption = scanner.nextInt() - 1;
        scanner.nextLine(); // consume newline
        
        questions.add(new MCQQuestion(questionId, topic, questionText, marks, options, correctOption));
        System.out.println("MCQ Question added successfully!");
    }
    
    private void addTrueFalseQuestion(Scanner scanner, List<Question> questions) {
        System.out.println("\nAdd True/False Question");
        System.out.print("Question ID: ");
        String questionId = scanner.nextLine();
        System.out.print("Topic: ");
        String topic = scanner.nextLine();
        System.out.print("Question Text: ");
        String questionText = scanner.nextLine();
        System.out.print("Marks: ");
        int marks = scanner.nextInt();
        scanner.nextLine(); // consume newline
        System.out.print("Is the statement true? (true/false): ");
        boolean correctAnswer = scanner.nextBoolean();
        scanner.nextLine(); // consume newline
        
        questions.add(new TrueFalseQuestion(questionId, topic, questionText, marks, correctAnswer));
        System.out.println("True/False Question added successfully!");
    }
    
   private void addSubjectiveQuestion(Scanner scanner, List<Question> questions) {
    System.out.println("\nAdd Subjective Question");
    System.out.print("Question ID: ");
    String questionId = scanner.nextLine();
    System.out.print("Topic: ");
    String topic = scanner.nextLine();
    System.out.print("Question Text: ");
    String questionText = scanner.nextLine();
    
    System.out.print("Marks: ");
    int marks = 0;
    try {
        marks = scanner.nextInt();
        scanner.nextLine(); // consume newline
    } catch (InputMismatchException e) {
        System.out.println("Invalid input! Please enter a number for marks.");
        scanner.nextLine();
        return;
    }
    
    System.out.print("Model Answer: ");
    String modelAnswer = scanner.nextLine();
    
    questions.add(new SubjectiveQuestion(questionId, topic, questionText, marks, modelAnswer));
    System.out.println("Subjective Question added successfully!");
}
    
    private void viewQuestions(List<Question> questions) {
        System.out.println("\nQuestions in Bank:");
        for (Question question : questions) {
            System.out.println("\nID: " + question.getQuestionId());
            System.out.println("Topic: " + question.getTopic());
            System.out.println("Marks: " + question.getMarks());
            question.displayQuestion();
        }
    }
    
    public void createQuiz(Teacher teacher) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\nCreate Quiz");
        
        System.out.println("Your Courses:");
        for (Course course : courses.values()) {
            if (course.getInstructor().equals(teacher)) {
                System.out.println(course.getCourseId() + " - " + course.getCourseName());
            }
        }
        
        System.out.print("Enter Course ID: ");
        String courseId = scanner.nextLine();
        
        Course course = courses.get(courseId);
        if (course == null || !course.getInstructor().equals(teacher)) {
            System.out.println("Invalid course selection!");
            return;
        }
        
        List<Question> availableQuestions = questionBanks.get(courseId);
        if (availableQuestions == null || availableQuestions.isEmpty()) {
            System.out.println("No questions available in the question bank for this course!");
            return;
        }
        
        System.out.print("Enter Quiz ID: ");
        String quizId = scanner.nextLine();
        
        System.out.print("Enter Quiz Date/Time (yyyy-MM-dd HH:mm): ");
        String dateTimeStr = scanner.nextLine();
        LocalDateTime startTime;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            startTime = LocalDateTime.parse(dateTimeStr, formatter);
        } catch (Exception e) {
            System.out.println("Invalid date/time format!");
            return;
        }
        
        System.out.print("Enter Quiz Duration (minutes): ");
        int durationMinutes = scanner.nextInt();
        Duration duration = Duration.ofMinutes(durationMinutes);
        scanner.nextLine(); // consume newline
        
        // Select questions
        List<Question> quizQuestions = new ArrayList<>();
        System.out.println("\nSelect Questions for Quiz:");
        
        // Group questions by topic
        Map<String, List<Question>> questionsByTopic = new HashMap<>();
        for (Question q : availableQuestions) {
            questionsByTopic.computeIfAbsent(q.getTopic(), k -> new ArrayList<>()).add(q);
        }
        
        for (String topic : questionsByTopic.keySet()) {
            System.out.println("\nTopic: " + topic);
            List<Question> topicQuestions = questionsByTopic.get(topic);
            for (int i = 0; i < topicQuestions.size(); i++) {
                System.out.println((i+1) + ". " + topicQuestions.get(i).getQuestionText() + 
                                 " (" + topicQuestions.get(i).getMarks() + " marks)");
            }
            
            System.out.print("Select questions from this topic (e.g., 1,3 or 'all'): ");
            String selection = scanner.nextLine();
            
            if (selection.equalsIgnoreCase("all")) {
                quizQuestions.addAll(topicQuestions);
            } else {
                String[] indices = selection.split(",");
                for (String indexStr : indices) {
                    try {
                        int index = Integer.parseInt(indexStr.trim()) - 1;
                        if (index >= 0 && index < topicQuestions.size()) {
                            quizQuestions.add(topicQuestions.get(index));
                        }
                    } catch (NumberFormatException e) {
                        // skip invalid entries
                    }
                }
            }
        }
        
        if (quizQuestions.isEmpty()) {
            System.out.println("No questions selected for the quiz!");
            return;
        }
        
        Quiz quiz = new Quiz(quizId, courseId, teacher, startTime, duration, quizQuestions);
        course.addQuiz(quiz);
        System.out.println("Quiz created successfully!");
    }
    
    public void attemptQuiz(Student student) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\nAttempt Quiz");
        
        System.out.println("Your Courses:");
        for (Course course : courses.values()) {
            if (course.getEnrolledStudents().contains(student)) {
                System.out.println(course.getCourseId() + " - " + course.getCourseName());
            }
        }
        
        System.out.print("Enter Course ID: ");
        String courseId = scanner.nextLine();
        
        Course course = courses.get(courseId);
        if (course == null || !course.getEnrolledStudents().contains(student)) {
            System.out.println("Invalid course selection!");
            return;
        }
        
        System.out.println("\nAvailable Quizzes:");
        List<Quiz> availableQuizzes = new ArrayList<>();
        for (Quiz quiz : course.getQuizzes()) {
            if (quiz.getAttempt(student.getUserId()) == null) {
                System.out.println(quiz.getQuizId() + " - Starts at: " + quiz.getStartTime() + 
                                 ", Duration: " + quiz.getDuration().toMinutes() + " minutes");
                availableQuizzes.add(quiz);
            }
        }
        
        if (availableQuizzes.isEmpty()) {
            System.out.println("No quizzes available to attempt!");
            return;
        }
        
        System.out.print("Enter Quiz ID to attempt: ");
        String quizId = scanner.nextLine();
        
        Quiz quiz = null;
        for (Quiz q : availableQuizzes) {
            if (q.getQuizId().equals(quizId)) {
                quiz = q;
                break;
            }
        }
        
        if (quiz == null) {
            System.out.println("Invalid quiz selection!");
            return;
        }
        
        if (!quiz.isActive()) {
            System.out.println("This quiz is not currently active!");
            return;
        }
        
        QuizAttempt attempt = quiz.attemptQuiz(student);
        if (attempt == null) return;
        
        System.out.println("\nStarting Quiz: " + quizId);
        System.out.println("You have " + quiz.getDuration().toMinutes() + " minutes to complete the quiz.");
        
        // Shuffle questions and options for security
        quiz.shuffleQuestions();
        for (Question question : quiz.getQuestions()) {
            if (question instanceof MCQQuestion mCQQuestion) {
                mCQQuestion.shuffleOptions();
            }
        }
        
        // Record start time
        Instant startTime = Instant.now();
        
        // Display and answer questions
        for (Question question : quiz.getQuestions()) {
            System.out.println("\nQuestion (" + question.getMarks() + " marks):");
            question.displayQuestion();
            
            System.out.print("Your answer: ");
            String answer = scanner.nextLine();
            
            attempt.recordAnswer(question.getQuestionId(), answer);
            
            // Check if time is up
            if (Instant.now().isAfter(startTime.plus(quiz.getDuration()))) {
                System.out.println("\nTime's up! Quiz auto-submitted.");
                break;
            }
        }
        
        attempt.gradeQuiz();
        System.out.println("\nQuiz submitted successfully!");
        System.out.println("Your score: " + attempt.getScore());
    }
    
    public void viewQuizResults(Teacher teacher) {
        System.out.println("\nView Quiz Results");
        
        System.out.println("Your Courses:");
        for (Course course : courses.values()) {
            if (course.getInstructor().equals(teacher)) {
                System.out.println(course.getCourseId() + " - " + course.getCourseName());
            }
        }
        
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Course ID: ");
        String courseId = scanner.nextLine();
        
        Course course = courses.get(courseId);
        if (course == null || !course.getInstructor().equals(teacher)) {
            System.out.println("Invalid course selection!");
            return;
        }
        
        if (course.getQuizzes().isEmpty()) {
            System.out.println("No quizzes available for this course!");
            return;
        }
        
        System.out.println("\nQuizzes:");
        for (Quiz quiz : course.getQuizzes()) {
            System.out.println(quiz.getQuizId() + " - " + quiz.getStartTime());
        }
        
        System.out.print("Enter Quiz ID: ");
        String quizId = scanner.nextLine();
        
        Quiz quiz = course.getQuiz(quizId);
        if (quiz == null) {
            System.out.println("Invalid quiz selection!");
            return;
        }
        
        System.out.println("\nResults for Quiz: " + quizId);
        System.out.println("Student\t\tScore");
        
        Map<String, QuizAttempt> attempts = quiz.getAttempts();
        for (Student student : course.getEnrolledStudents()) {
            QuizAttempt attempt = attempts.get(student.getUserId());
            System.out.println(student.getName() + "\t\t" + (attempt != null ? attempt.getScore() : "Absent"));
        }
        
        // Save to file
        saveResultsToFile(course, quiz);
    }
    
    private void saveResultsToFile(Course course, Quiz quiz) {
        String filename = "results_" + course.getCourseId() + "_" + quiz.getQuizId() + ".txt";
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("Results for " + course.getCourseName() + " - Quiz: " + quiz.getQuizId());
            writer.println("Date: " + LocalDate.now());
            writer.println("\nStudent\t\tScore");
            
            Map<String, QuizAttempt> attempts = quiz.getAttempts();
            for (Student student : course.getEnrolledStudents()) {
                QuizAttempt attempt = attempts.get(student.getUserId());
                writer.println(student.getName() + "\t\t" + (attempt != null ? attempt.getScore() : "Absent"));
            }
            
            System.out.println("Results saved to file: " + filename);
        } catch (IOException e) {
            System.out.println("Error saving results to file!");
        }
    }
    
    public void viewAnalytics(Teacher teacher) {
        System.out.println("\nView Quiz Analytics");
        
        System.out.println("Your Courses:");
        for (Course course : courses.values()) {
            if (course.getInstructor().equals(teacher)) {
                System.out.println(course.getCourseId() + " - " + course.getCourseName());
            }
        }
        
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Course ID: ");
        String courseId = scanner.nextLine();
        
        Course course = courses.get(courseId);
        if (course == null || !course.getInstructor().equals(teacher)) {
            System.out.println("Invalid course selection!");
            return;
        }
        
        if (course.getQuizzes().isEmpty()) {
            System.out.println("No quizzes available for this course!");
            return;
        }
        
        System.out.println("\nQuizzes:");
        for (Quiz quiz : course.getQuizzes()) {
            System.out.println(quiz.getQuizId() + " - " + quiz.getStartTime());
        }
        
        System.out.print("Enter Quiz ID: ");
        String quizId = scanner.nextLine();
        
        Quiz quiz = course.getQuiz(quizId);
        if (quiz == null) {
            System.out.println("Invalid quiz selection!");
            return;
        }
        
        System.out.println("\nAnalytics for Quiz: " + quizId);
        
        Map<String, QuizAttempt> attempts = quiz.getAttempts();
        int totalStudents = course.getEnrolledStudents().size();
        int attempted = attempts.size();
        
        System.out.println("Participation: " + attempted + "/" + totalStudents + " (" + 
                         (attempted * 100 / totalStudents) + "%)");
        
        System.out.println("\nQuestion-wise Performance:");
        for (Question question : quiz.getQuestions()) {
            int correct = 0;
            for (QuizAttempt attempt : attempts.values()) {
                String answer;
                answer = attempt.getQuiz().getAttempt(attempt.getStudent().getUserId())
                        .answers.get(question.getQuestionId());
                if (answer != null && question.checkAnswer(answer)) {
                    correct++;
                }
            }
            
            int percentage = attempted > 0 ? (correct * 100 / attempted) : 0;
            System.out.println("\nQuestion: " + question.getQuestionText());
            System.out.println("Correct: " + correct + "/" + attempted + " (" + percentage + "%)");
            System.out.println(createBarChart(percentage));
        }
    }
    
    private String createBarChart(int percentage) {
        int bars = percentage / 5;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bars; i++) {
            sb.append("|");
        }
        return sb.toString();
    }
    
    public void viewAttendance(Teacher teacher) {
        System.out.println("\nView Quiz Attendance");
        
        System.out.println("Your Courses:");
        for (Course course : courses.values()) {
            if (course.getInstructor().equals(teacher)) {
                System.out.println(course.getCourseId() + " - " + course.getCourseName());
            }
        }
        
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Course ID: ");
        String courseId = scanner.nextLine();
        
        Course course = courses.get(courseId);
        if (course == null || !course.getInstructor().equals(teacher)) {
            System.out.println("Invalid course selection!");
            return;
        }
        
        if (course.getQuizzes().isEmpty()) {
            System.out.println("No quizzes available for this course!");
            return;
        }
        
        System.out.println("\nQuizzes:");
        for (Quiz quiz : course.getQuizzes()) {
            System.out.println(quiz.getQuizId() + " - " + quiz.getStartTime());
        }
        
        System.out.print("Enter Quiz ID: ");
        String quizId = scanner.nextLine();
        
        Quiz quiz = course.getQuiz(quizId);
        if (quiz == null) {
            System.out.println("Invalid quiz selection!");
            return;
        }
        
        System.out.println("\nAttendance for Quiz: " + quizId);
        System.out.println("Student\t\tStatus");
        
        Map<String, QuizAttempt> attempts = quiz.getAttempts();
        for (Student student : course.getEnrolledStudents()) {
            System.out.println(student.getName() + "\t\t" + 
                             (attempts.containsKey(student.getUserId()) ? "Present" : "Absent"));
        }
        
        // Save to file
        saveAttendanceToFile(course, quiz);
    }
    
    private void saveAttendanceToFile(Course course, Quiz quiz) {
        String filename = "attendance_" + course.getCourseId() + "_" + quiz.getQuizId() + ".txt";
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("Attendance for " + course.getCourseName() + " - Quiz: " + quiz.getQuizId());
            writer.println("Date: " + LocalDate.now());
            writer.println("\nStudent\t\tStatus");
            
            Map<String, QuizAttempt> attempts = quiz.getAttempts();
            for (Student student : course.getEnrolledStudents()) {
                writer.println(student.getName() + "\t\t" + 
                             (attempts.containsKey(student.getUserId()) ? "Present" : "Absent"));
            }
            
            System.out.println("Attendance saved to file: " + filename);
        } catch (IOException e) {
            System.out.println("Error saving attendance to file!");
        }
    }
    
    public void viewStudentResults(Student student) {
        System.out.println("\nYour Quiz Results");
        
        System.out.println("Your Courses:");
        for (Course course : courses.values()) {
            if (course.getEnrolledStudents().contains(student)) {
                System.out.println(course.getCourseId() + " - " + course.getCourseName());
            }
        }
        
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Course ID: ");
        String courseId = scanner.nextLine();
        
        Course course = courses.get(courseId);
        if (course == null || !course.getEnrolledStudents().contains(student)) {
            System.out.println("Invalid course selection!");
            return;
        }
        
        System.out.println("\nYour Quiz Attempts:");
        boolean hasResults = false;
        for (Quiz quiz : course.getQuizzes()) {
            QuizAttempt attempt = quiz.getAttempt(student.getUserId());
            if (attempt != null) {
                System.out.println("Quiz: " + quiz.getQuizId() + ", Score: " + attempt.getScore());
                hasResults = true;
            }
        }
        
        if (!hasResults) {
            System.out.println("No quiz results available!");
        }
    }
    
    public void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("system_data.ser"))) {
            oos.writeObject(users);
            oos.writeObject(courses);
            oos.writeObject(questionBanks);
            System.out.println("System data saved successfully!");
        } catch (IOException e) {
            System.out.println("Error saving system data!");
        }
    }
    
    @SuppressWarnings("unchecked")
    public void loadData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("system_data.ser"))) {
            users = (Map<String, User>) ois.readObject();
            courses = (Map<String, Course>) ois.readObject();
            questionBanks = (Map<String, List<Question>>) ois.readObject();
            System.out.println("System data loaded successfully!");
        } catch (FileNotFoundException e) {
            System.out.println("No saved data found. Starting with sample data.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading system data!");
        }
    }
    
    public static void main(String[] args) {
        ExaminationSystem system = new ExaminationSystem();
        system.loadData();
        
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\nExamination System");
            System.out.println("1. Login");
            System.out.println("2. Exit");
            
            System.out.print("Enter choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline
            
            switch (choice) {
                case 1:
                    system.login();
                    break;
                case 2:
                    system.saveData();
                    System.out.println("Goodbye!");
                    System.exit(0);
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }
}