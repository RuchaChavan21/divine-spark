package com.DivineSpark.service;

import com.DivineSpark.dto.DonationDTO;

import java.util.List;

public interface DonationService {

    /**
     * Creates a new donation record.
     * @param donationDTO The donation data transfer object.
     * @return The created donation DTO.
     */
    DonationDTO createDonation(DonationDTO donationDTO);

    /**
     * Retrieves a donation by its ID.
     * @param id The ID of the donation (should be Long to match the entity).
     * @return The found donation DTO.
     */
    DonationDTO getDonationById(Long id);

    /**
     * Retrieves all donations.
     * @return A list of all donation DTOs.
     */
    List<DonationDTO> getAllDonations();

    /**
     * Updates an existing donation.
     * @param id The ID of the donation to update.
     * @param donationDTO The updated donation data.
     * @return The updated donation DTO.
     */
    DonationDTO updateDonation(Long id, DonationDTO donationDTO);

    /**
     * Deletes a donation by its ID.
     * @param id The ID of the donation to delete.
     */
    void deleteDonation(Long id);

}