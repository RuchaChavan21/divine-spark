package com.DivineSpark.ServiceImpl;

import com.DivineSpark.dto.DonationDTO;
import com.DivineSpark.model.Donation;
import com.DivineSpark.repository.DonationRepository;
import com.DivineSpark.service.DonationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DonationServiceImpl implements DonationService {

    private final DonationRepository donationRepository;

    @Override
    public DonationDTO createDonation(DonationDTO donationDTO) {
        Donation donation = mapToEntity(donationDTO);
        Donation savedDonation = donationRepository.save(donation);
        return mapToDTO(savedDonation);
    }

    @Override
    public DonationDTO getDonationById(Long donationId) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new EntityNotFoundException("Donation not found with id: " + donationId));
        return mapToDTO(donation);
    }

    @Override
    public List<DonationDTO> getAllDonations() {
        List<Donation> donations = donationRepository.findAll();
        return donations.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public DonationDTO updateDonation(Long donationId, DonationDTO donationDTO) {
        Donation existingDonation = donationRepository.findById(donationId)
                .orElseThrow(() -> new EntityNotFoundException("Donation not found with id: " + donationId));

        // Update fields
        existingDonation.setFirstName(donationDTO.getFirstName());
        existingDonation.setLastName(donationDTO.getLastName());
        existingDonation.setEmail(donationDTO.getEmail());
        existingDonation.setPhone(donationDTO.getPhone());
        existingDonation.setTowards(donationDTO.getTowards());
        existingDonation.setAmount(donationDTO.getAmount());

        Donation updatedDonation = donationRepository.save(existingDonation);
        return mapToDTO(updatedDonation);
    }

    @Override
    public void deleteDonation(Long donationId) {
        if (!donationRepository.existsById(donationId)) {
            throw new EntityNotFoundException("Donation not found with id: " + donationId);
        }
        donationRepository.deleteById(donationId);
    }

    // --- Helper Methods for Mapping ---

    private DonationDTO mapToDTO(Donation donation) {
        DonationDTO dto = new DonationDTO();
        dto.setId(donation.getId());
        dto.setFirstName(donation.getFirstName());
        dto.setLastName(donation.getLastName());
        dto.setEmail(donation.getEmail());
        dto.setPhone(donation.getPhone());
        dto.setTowards(donation.getTowards());
        dto.setAmount(donation.getAmount());
        dto.setCreatedAt(donation.getCreatedAt());
        dto.setUpdatedAt(donation.getUpdatedAt());
        return dto;
    }

    private Donation mapToEntity(DonationDTO dto) {
        Donation donation = new Donation();
        // We don't set the ID for a new entity, it's generated automatically
        donation.setFirstName(dto.getFirstName());
        donation.setLastName(dto.getLastName());
        donation.setEmail(dto.getEmail());
        donation.setPhone(dto.getPhone());
        donation.setTowards(dto.getTowards());
        donation.setAmount(dto.getAmount());
        // createdAt and updatedAt are handled automatically by JPA
        return donation;
    }
}