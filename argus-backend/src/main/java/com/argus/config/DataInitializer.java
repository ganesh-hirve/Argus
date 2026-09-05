package com.argus.config;

import com.argus.entity.TaskAuthority;
import com.argus.entity.TaskResource;
import com.argus.enums.TaskStatus;
import com.argus.repository.TaskAuthorityRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner init(TaskAuthorityRepository repository) {

        return args -> {

            if (repository.findByTaskId("TASK-DEMO-001").isEmpty()) {

                // Create Task Authority
                TaskAuthority task = new TaskAuthority();

                task.setTaskId("TASK-DEMO-001");
                task.setAgentId("AGENT-001");
                task.setStatus(TaskStatus.ACTIVE);
                task.setMaxBudget(5_000_000L); // ₹50,000 in paise
                task.setConsumedBudget(0L);
                task.setReservedBudget(0L);
                task.setExpiresAt(LocalDateTime.now().plusHours(24));


                // Create Resource 1
                TaskResource resource1 = new TaskResource();

                resource1.setResourceType("INVOICE");
                resource1.setResourceId("INV-101");
                resource1.setVendor("AWS");
                resource1.setAmount(1_500_000L); // ₹15,000


                // Create Resource 2
                TaskResource resource2 = new TaskResource();

                resource2.setResourceType("INVOICE");
                resource2.setResourceId("INV-102");
                resource2.setVendor("AWS");
                resource2.setAmount(2_000_000L); // ₹20,000


                // Establish bidirectional relationship
                task.addResource(resource1);
                task.addResource(resource2);


                // Cascade saves resources automatically
                repository.save(task);

                System.out.println("======================================");
                System.out.println("ARGUS Demo Task Authority Created");
                System.out.println("Task ID: " + task.getTaskId());
                System.out.println("Resources: " + task.getResources().size());
                System.out.println("Budget: ₹" + task.getMaxBudget() / 100);
                System.out.println("======================================");
            }
        };
    }
}