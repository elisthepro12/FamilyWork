package com.example.familywork;

public class FamilyTask {
    private String taskName;
    private String assignedTo; // שם בן המשפחה
    private String dayOfWeek;  // למשל: "יום ראשון"
    private int color;         // צבע ייחודי לכל בן משפחה

    public FamilyTask(String taskName, String assignedTo, String dayOfWeek, int color) {
        this.taskName = taskName;
        this.assignedTo = assignedTo;
        this.dayOfWeek = dayOfWeek;
        this.color = color;
    }

    // Getters
    public String getTaskName() { return taskName; }
    public String getAssignedTo() { return assignedTo; }
    public String getDayOfWeek() { return dayOfWeek; }
    public int getColor() { return color; }
}