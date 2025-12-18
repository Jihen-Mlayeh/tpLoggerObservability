package com.example.logging_and_observability.profiling.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for user profiles
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "profileType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ReadHeavyProfile.class, name = "READ_HEAVY"),
        @JsonSubTypes.Type(value = WriteHeavyProfile.class, name = "WRITE_HEAVY"),
        @JsonSubTypes.Type(value = ExpensiveProductSeekerProfile.class, name = "EXPENSIVE_SEEKER")
})
public abstract class UserProfile {

    protected String userName;
    protected String userEmail;
    protected int userAge;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    protected LocalDateTime profileCreatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    protected LocalDateTime lastActivityAt;

    protected int totalOperations;
    protected List<OperationLog> operationHistory = new ArrayList<>();

    public abstract String getProfileType();
    public abstract String getProfileDescription();
}
