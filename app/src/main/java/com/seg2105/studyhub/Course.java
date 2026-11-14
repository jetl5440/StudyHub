package com.seg2105.studyhub;

// A course that a student is taking. Each course belongs to one student (the owner).
public class Course {

    private int id;
    private String ownerUsername;
    private String code;      // e.g. SEG2105
    private String title;     // e.g. Introduction to Software Engineering
    private int categoryId;   // one of the categories created by the admin
    private String term;      // e.g. Fall 2026
    private String location;  // optional, can be empty

    public Course(int id, String ownerUsername, String code, String title, int categoryId,
                  String term, String location) {
        this.id = id;
        this.ownerUsername = ownerUsername;
        this.code = code;
        this.title = title;
        this.categoryId = categoryId;
        this.term = term;
        this.location = location;
    }

    public int getId() { return id; }
    public String getOwnerUsername() { return ownerUsername; }
    public String getCode() { return code; }
    public String getTitle() { return title; }
    public int getCategoryId() { return categoryId; }
    public String getTerm() { return term; }
    public String getLocation() { return location; }
}
