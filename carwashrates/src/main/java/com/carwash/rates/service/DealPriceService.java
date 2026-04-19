package com.carwash.rates.service;

import com.carwash.rates.dto.DealPriceDTO;
import com.carwash.rates.entity.DealPrice;
import com.carwash.rates.repository.DealPriceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
            dealPrice.getDealFinalPrice(),
            dealPrice.getTotalMonths()
        );
    }
    
    // Get all deal prices
    @Cacheable(value = "dealPrices", key = "'all'")
    public List<DealPriceDTO> getAllDealPrices() {
        return dealPriceRepository.findAll()
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    // Get deal prices by service type
    @Cacheable(value = "dealPrices", key = "'serviceType:' + #serviceType")
    public List<DealPriceDTO> getDealPricesByServiceType(String serviceType) {
        return dealPriceRepository.findByDealServiceType(serviceType)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    // Get deal prices by wash type
    @Cacheable(value = "dealPrices", key = "'washType:' + #washType")
    public List<DealPriceDTO> getDealPricesByWashType(String washType) {
        return dealPriceRepository.findByDealWashType(washType)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    // Get deal prices by car type
    @Cacheable(value = "dealPrices", key = "'carType:' + #carType")
    public List<DealPriceDTO> getDealPricesByCarType(String carType) {
        return dealPriceRepository.findByDealCarType(carType)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    // Get deal prices by service type and wash type
    @Cacheable(value = "dealPrices", key = "'st:' + #serviceType + ':wt:' + #washType")
    public List<DealPriceDTO> getDealPricesByServiceTypeAndWashType(String serviceType, String washType) {
        return dealPriceRepository.findByDealServiceTypeAndDealWashType(serviceType, washType)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    // Get deal prices by service type and car type
    @Cacheable(value = "dealPrices", key = "'st:' + #serviceType + ':ct:' + #carType")
    public List<DealPriceDTO> getDealPricesByServiceTypeAndCarType(String serviceType, String carType) {
        return dealPriceRepository.findByDealServiceTypeAndDealCarType(serviceType, carType)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    // Get deal prices by service type, wash type and car type
    @Cacheable(value = "dealPrices", key = "'st:' + #serviceType + ':wt:' + #washType + ':ct:' + #carType")
    public List<DealPriceDTO> getDealPricesByServiceTypeAndWashTypeAndCarType(String serviceType, String washType, String carType) {
        return dealPriceRepository.findByDealServiceTypeAndDealWashTypeAndDealCarType(serviceType, washType, carType)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    // Get deal price by ID
    @Cacheable(value = "dealPrices", key = "'id:' + #id")
    public DealPriceDTO getDealPriceById(Long id) {
        return dealPriceRepository.findById(id)
            .map(this::convertToDTO)
            .orElse(null);
    }
    
    // Create new deal price
    @CacheEvict(value = "dealPrices", allEntries = true)
    public DealPriceDTO createDealPrice(DealPrice dealPrice) {
        DealPrice saved = dealPriceRepository.save(dealPrice);
        return convertToDTO(saved);
    }
    
    // Update deal price
    @CacheEvict(value = "dealPrices", allEntries = true)
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
                existing.setTotalMonths(dealPrice.getTotalMonths());
                DealPrice updated = dealPriceRepository.save(existing);
                return convertToDTO(updated);
            })
            .orElse(null);
    }
    
    // Delete deal price
    @CacheEvict(value = "dealPrices", allEntries = true)
    public boolean deleteDealPrice(Long id) {
        if (dealPriceRepository.existsById(id)) {
            dealPriceRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
