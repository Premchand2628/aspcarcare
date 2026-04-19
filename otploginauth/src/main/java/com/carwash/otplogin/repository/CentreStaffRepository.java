package com.carwash.otplogin.repository;

import com.carwash.otplogin.entity.CentreStaff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CentreStaffRepository extends JpaRepository<CentreStaff, UUID> {

    Optional<CentreStaff> findByPhoneAndIsActiveTrue(String phone);

    boolean existsByPhone(String phone);
}
