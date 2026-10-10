package com.app.service;

import com.app.dto.PeopleResponse;
import com.app.repository.DonorRepository;
import com.app.repository.VolunteerRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PeopleService {

    private final DonorRepository donorRepository;
    private final VolunteerRepository volunteerRepository;

    public PeopleService(DonorRepository donorRepository, VolunteerRepository volunteerRepository) {
        this.donorRepository = donorRepository;
        this.volunteerRepository = volunteerRepository;
    }

    @Transactional(readOnly = true)
    public PeopleResponse getAll() {
        return new PeopleResponse(
                donorRepository.findAll(Sort.by("id")).stream().map(PeopleResponse.DonorDto::from).toList(),
                volunteerRepository.findAll(Sort.by("id")).stream().map(PeopleResponse.VolunteerDto::from).toList());
    }
}
