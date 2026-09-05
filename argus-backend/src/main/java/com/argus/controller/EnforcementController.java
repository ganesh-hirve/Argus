package com.argus.controller;

import com.argus.dto.ExecutionRequest;
import com.argus.dto.ExecutionResponse;
import com.argus.service.EnforcementService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/argus")
@CrossOrigin(origins = "*")
public class EnforcementController {

    private final EnforcementService enforcementService;

    public EnforcementController(
            EnforcementService enforcementService
    ) {
        this.enforcementService = enforcementService;
    }


    // =========================================================
    // DRY RUN
    // Evaluates request without modifying database state
    // =========================================================

    @PostMapping("/evaluate")
    public ResponseEntity<ExecutionResponse> evaluate(
            @RequestBody ExecutionRequest request
    ) {

        ExecutionResponse response =
                enforcementService.evaluate(request);

        return ResponseEntity.ok(response);
    }


    // =========================================================
    // ACTUAL EXECUTION
    // Evaluates + enforces + modifies system state
    // =========================================================

    @PostMapping("/execute")
    public ResponseEntity<ExecutionResponse> execute(
            @RequestBody ExecutionRequest request
    ) {

        ExecutionResponse response =
                enforcementService.execute(request);

        return ResponseEntity.ok(response);
    }
}