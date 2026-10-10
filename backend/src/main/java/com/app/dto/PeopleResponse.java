package com.app.dto;

import com.app.model.Donor;
import com.app.model.Volunteer;

import java.util.List;

public record PeopleResponse(List<DonorDto> donors, List<VolunteerDto> volunteers) {

    public record DonorDto(String id, String name, String zone) {
        public static DonorDto from(Donor donor) {
            return new DonorDto(donor.getId(), donor.getName(), donor.getZone());
        }
    }

    public record VolunteerDto(String id, String name, int capacityKg, List<String> zones) {
        public static VolunteerDto from(Volunteer volunteer) {
            return new VolunteerDto(volunteer.getId(), volunteer.getName(), volunteer.getCapacityKg(),
                    volunteer.getZones().stream().sorted().toList());
        }
    }
}
