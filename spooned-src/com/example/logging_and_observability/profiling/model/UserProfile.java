package com.example.logging_and_observability.profiling.model;
import JsonSubTypes.Type;
import JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * Base class for user profiles
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = Id.NAME, property = "profileType")
@JsonSubTypes({ @Type(value = ReadHeavyProfile.class, name = "READ_HEAVY"), @Type(value = WriteHeavyProfile.class, name = "WRITE_HEAVY"), @Type(value = ExpensiveProductSeekerProfile.class, name = "EXPENSIVE_SEEKER") })
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