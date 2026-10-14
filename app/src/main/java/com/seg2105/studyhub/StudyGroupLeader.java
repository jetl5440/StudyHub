package com.seg2105.studyhub;

// A study group leader is a student who can also create study groups (later deliverables)
public class StudyGroupLeader extends Student {

    public StudyGroupLeader(String firstName, String lastName, String email, String username,
                            String password, String studentNumber) {
        super(firstName, lastName, email, username, password, studentNumber);
    }

    @Override
    public String getRole() {
        return "Study Group Leader";
    }
}
