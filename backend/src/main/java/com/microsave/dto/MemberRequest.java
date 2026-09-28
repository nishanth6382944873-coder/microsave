package com.microsave.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MemberRequest {

    @NotBlank(message = "Member name must not be empty")
    private String name;

    @NotBlank(message = "Phone number must not be empty")
    private String phone;

    @Email(message = "Email should be valid if provided")
    private String email;

    private String address;

    @NotNull(message = "Group ID is required")
    private Long groupId;

    public MemberRequest() {
    }

    public MemberRequest(String name, String phone, String email, String address, Long groupId) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }
}
