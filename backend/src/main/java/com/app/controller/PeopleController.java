package com.app.controller;

import com.app.dto.PeopleResponse;
import com.app.service.PeopleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PeopleController {

    private final PeopleService peopleService;

    public PeopleController(PeopleService peopleService) {
        this.peopleService = peopleService;
    }

    @GetMapping("/api/people")
    public PeopleResponse getAll() {
        return peopleService.getAll();
    }
}
