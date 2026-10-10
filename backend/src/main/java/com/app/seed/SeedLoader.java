package com.app.seed;

import com.app.model.AppClock;
import com.app.model.Donation;
import com.app.model.Donor;
import com.app.model.Volunteer;
import com.app.repository.AppClockRepository;
import com.app.repository.DonationRepository;
import com.app.repository.DonorRepository;
import com.app.repository.VolunteerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class SeedLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedLoader.class);

    private final ObjectMapper objectMapper;
    private final AppClockRepository clockRepository;
    private final DonorRepository donorRepository;
    private final VolunteerRepository volunteerRepository;
    private final DonationRepository donationRepository;

    public SeedLoader(ObjectMapper objectMapper, AppClockRepository clockRepository, DonorRepository donorRepository,
                      VolunteerRepository volunteerRepository, DonationRepository donationRepository) {
        this.objectMapper = objectMapper;
        this.clockRepository = clockRepository;
        this.donorRepository = donorRepository;
        this.volunteerRepository = volunteerRepository;
        this.donationRepository = donationRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws IOException {
        if (clockRepository.existsById(AppClock.SINGLETON_ID)) {
            log.info("Saved state found, skipping seed");
            return;
        }

        SeedData seed;
        try (InputStream in = new ClassPathResource("seed.json").getInputStream()) {
            seed = objectMapper.readValue(in, SeedData.class);
        }

        Map<String, Donor> donors = seed.donors().stream()
                .map(d -> new Donor(d.id(), d.name(), d.zone()))
                .map(donorRepository::save)
                .collect(Collectors.toMap(Donor::getId, Function.identity()));

        seed.volunteers().forEach(v -> volunteerRepository.save(
                new Volunteer(v.id(), v.name(), v.capacityKg(), new HashSet<>(v.zones()))));

        seed.donations().forEach(d -> donationRepository.save(new Donation(
                d.id(), donors.get(d.donorId()), d.description(), d.quantityKg(), d.pickupFrom(), d.pickupUntil())));

        clockRepository.save(new AppClock(seed.clock()));
        log.info("Loaded seed data: {} donors, {} volunteers, {} donations",
                seed.donors().size(), seed.volunteers().size(), seed.donations().size());
    }
}
