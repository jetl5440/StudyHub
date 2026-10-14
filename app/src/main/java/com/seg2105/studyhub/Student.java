package com.seg2105.studyhub;

public class Student extends User {

    private String studentNumber;

    public Student(String firstName, String lastName, String email, String username,
                   String password, String studentNumber) {
        super(firstName, lastName, email, username, password);
        this.studentNumber = studentNumber;
    }

    public String getStudentNumber() { return studentNumber; }

    @Override
    public String getRole() {
        return "Student";
    }
}
