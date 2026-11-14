package com.seg2105.studyhub;

// A task (assignment, reading, lab, ...) that belongs to one of a student's courses
public class Task {

    public static final String STATUS_TODO = "To Do";
    public static final String STATUS_IN_PROGRESS = "In Progress";
    public static final String STATUS_COMPLETED = "Completed";
    public static final String[] STATUSES = {STATUS_TODO, STATUS_IN_PROGRESS, STATUS_COMPLETED};

    private int id;
    private int courseId;
    private String ownerUsername;
    private String title;
    private String description;
    private String dueDate; // stored as yyyy-MM-dd so tasks sort by date
    private String status;

    public Task(int id, int courseId, String ownerUsername, String title, String description,
                String dueDate, String status) {
        this.id = id;
        this.courseId = courseId;
        this.ownerUsername = ownerUsername;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.status = status;
    }

    public int getId() { return id; }
    public int getCourseId() { return courseId; }
    public String getOwnerUsername() { return ownerUsername; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDueDate() { return dueDate; }
    public String getStatus() { return status; }

    public boolean isCompleted() {
        return STATUS_COMPLETED.equals(status);
    }
}
