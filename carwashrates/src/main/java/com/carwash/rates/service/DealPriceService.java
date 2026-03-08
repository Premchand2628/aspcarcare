package com.carwash.rates.service;

import com.carwash.rates.dto.DealPriceDTO;
import com.carwash.rates.entity.DealPrice;
import com.carwash.rates.repository.DealPriceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DealPriceService {
    
    @Autowired
    private DealPriceRepository dealPriceRepository;
    
    // Convert Entity to DTO
    private DealPriceDTO convertToDTO(DealPrice dealPrice) {
        return new DealPriceDTO(
            dealPrice.getId(),
            dealPrice.getDealServiceType(),
            dealPrice.getDealWashType(),
            dealPrice.getDealCarType(),
            dealPrice.getDealWaterProviding(),
            dealPrice.getDealActualPrice(),
            dealPrice.getDealDiscount(),
            dealPrice.getDealFinalPrice()
        );
    }
    
    // Get all deal prices
    public List<DealPriceDTO> getAllDealPrices() {
        return dealPriceRepository.findAll()
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    // Get deal prices by service type
    public List<DealPriceDTO> getDealPricesByServiceType(String serviceType) {
        return dealPriceRepository.findByDealServiceType(serviceType)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    // Get deal prices by wash type
    public List<DealPriceDTO> getDealPricesByWashType(String washType) {
        return dealPriceRepository.findByDealWashType(washType)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    // Get deal prices by car type
    public List<DealPriceDTO> getDealPricesByCarType(String carType) {
        return dealPriceRepository.findByDealCarType(carType)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    // Get deal prices by service type and wash type
    public List<DealPriceDTO> getDealPricesByServiceTypeAndWashType(String serviceType, String washType) {
        return dealPriceRepository.findByDealServiceTypeAndDealWashType(serviceType, washType)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    // Get deal prices by service type and car type
    public List<DealPriceDTO> getDealPricesByServiceTypeAndCarType(String serviceType, String carType) {
        return dealPriceRepository.findByDealServiceTypeAndDealCarType(serviceType, carType)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    // Get deal prices by service type, wash type and car type
    public List<DealPriceDTO> getDealPricesByServiceTypeAndWashTypeAndCarType(String serviceType, String washType, String carType) {
        return dealPriceRepository.findByDealServiceTypeAndDealWashTypeAndDealCarType(serviceType, washType, carType)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    // Get deal price by ID
    public DealPriceDTO getDealPriceById(Long id) {
        return dealPriceRepository.findById(id)
            .map(this::convertToDTO)
            .orElse(null);
    }
    
    // Create new deal price
    public DealPriceDTO createDealPrice(DealPrice dealPrice) {
        DealPrice saved = dealPriceRepository.save(dealPrice);
        return convertToDTO(saved);
    }
    
    // Update deal price
    public DealPriceDTO updateDealPrice(Long id, DealPrice dealPrice) {
        return dealPriceRepository.findById(id)
            .map(existing -> {
                existing.setDealServiceType(dealPrice.getDealServiceType());
                existing.setDealWashType(dealPrice.getDealWashType());
                existing.setDealCarType(dealPrice.getDealCarType());
                existing.setDealWaterProviding(dealPrice.getDealWaterProviding());
                existing.setDealActualPrice(dealPrice.getDealActualPrice());
                existing.setDealDiscount(dealPrice.getDealDiscount());
                existing.setDealFinalPrice(dealPrice.getDealFinalPrice());
                DealPrice updated = dealPriceRepository.save(existing);
                return convertToDTO(updated);
            })
            .orElse(null);
    }
    
    // Delete deal price
    public boolean deleteDealPrice(Long id) {
        if (dealPriceRepository.existsById(id)) {
            dealPriceRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
