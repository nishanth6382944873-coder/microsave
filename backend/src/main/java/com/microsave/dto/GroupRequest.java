package com.microsave.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public class GroupRequest {

    @NotBlank(message = "Group name is required")
    private String name;

    private String description;

    private LocalDate createdDate;

    public GroupRequest() {
    }

    public GroupRequest(String name, String description, LocalDate createdDate) {
        this.name = name;
        this.description = description;
        this.createdDate = createdDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }
}
