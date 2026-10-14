package com.seg2105.studyhub;

public class Admin extends User {

    public Admin(String firstName, String lastName, String email, String username, String password) {
        super(firstName, lastName, email, username, password);
    }

    @Override
    public String getRole() {
        return "Admin";
    }
}
