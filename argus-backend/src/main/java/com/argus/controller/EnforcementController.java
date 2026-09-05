package com.argus.controller;

import com.argus.dto.ExecutionRequest;
import com.argus.dto.ExecutionResponse;
import com.argus.service.EnforcementService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/argus")
public class EnforcementController {

    private final EnforcementService enforcementService;

    public EnforcementController(EnforcementService enforcementService) {
        this.enforcementService = enforcementService;
    }


    // Phase 1: Check whether execution is authorized
    @PostMapping("/evaluate")
    public ResponseEntity<ExecutionResponse> evaluate(
            @RequestBody ExecutionRequest request
    ) {

        ExecutionResponse response =
                enforcementService.evaluate(request);

        return ResponseEntity.ok(response);
    }


    // Phase 2: Actually execute and change system state
    @PostMapping("/execute")
    public ResponseEntity<ExecutionResponse> execute(
            @RequestBody ExecutionRequest request
    ) {

        ExecutionResponse response =
                enforcementService.execute(request);

        return ResponseEntity.ok(response);
    }
}