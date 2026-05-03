package com.carwash.otplogin.repository;

import com.carwash.otplogin.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {

    List<UserAddress> findByUserIdAndActiveTrueOrderByDefaultAddressDescCreatedAtAsc(Long userId);

    Optional<UserAddress> findByIdAndUserIdAndActiveTrue(Long id, Long userId);

    @Modifying
    @Query("UPDATE UserAddress a SET a.defaultAddress = false WHERE a.userId = :userId AND a.id <> :keepId AND a.active = true")
    void clearOtherDefaults(@Param("userId") Long userId, @Param("keepId") Long keepId);

    @Modifying
    @Query("UPDATE UserAddress a SET a.defaultAddress = false WHERE a.userId = :userId AND a.active = true")
    void clearAllDefaults(@Param("userId") Long userId);
}
