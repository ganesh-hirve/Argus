package com.argus.entity;

import com.argus.enums.ResourceState;
import jakarta.persistence.*;

@Entity
@Table(
        name = "task_resource",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_task_resource",
                        columnNames = {
                                "task_authority_id",
                                "resource_type",
                                "resource_id"
                        }
                )
        }
)
public class TaskResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_authority_id", nullable = false)
    private TaskAuthority taskAuthority;

    @Column(name = "resource_type", nullable = false)
    private String resourceType;

    @Column(name = "resource_id", nullable = false)
    private String resourceId;

    @Column(nullable = false)
    private String vendor;

    // Amount stored in paise
    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceState state;


    // Required by JPA
    public TaskResource() {
    }


    // ---------------- GETTERS ----------------

    public Long getId() {
        return id;
    }

    public TaskAuthority getTaskAuthority() {
        return taskAuthority;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getVendor() {
        return vendor;
    }

    public Long getAmount() {
        return amount;
    }

    public ResourceState getState() {
        return state;
    }


    // ---------------- SETTERS ----------------

    public void setTaskAuthority(TaskAuthority taskAuthority) {
        this.taskAuthority = taskAuthority;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public void setState(ResourceState state) {
        this.state = state;
    }


    // ---------------- JPA LIFECYCLE ----------------

    @PrePersist
    public void prePersist() {
        if (state == null) {
            state = ResourceState.PENDING;
        }
    }
}