package com.app.controller;

import com.app.dto.ClockResponse;
import com.app.dto.ClockUpdateRequest;
import com.app.service.ClockService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clock")
public class ClockController {

    private final ClockService clockService;

    public ClockController(ClockService clockService) {
        this.clockService = clockService;
    }

    @GetMapping
    public ClockResponse get() {
        return new ClockResponse(clockService.now());
    }

    @PutMapping
    public ClockResponse move(@Valid @RequestBody ClockUpdateRequest request) {
        return new ClockResponse(clockService.moveTo(request.now()));
    }
}
