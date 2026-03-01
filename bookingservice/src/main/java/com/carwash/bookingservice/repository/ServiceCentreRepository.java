package com.carwash.bookingservice.repository;

import com.carwash.bookingservice.entity.ServiceCentre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServiceCentreRepository extends JpaRepository<ServiceCentre, Long> {

    // simple search by area / name / address
    @Query("""
           SELECT c FROM ServiceCentre c
           WHERE c.active = TRUE
             AND (
                   LOWER(c.area)    LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(c.name)    LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(c.address) LIKE LOWER(CONCAT('%', :q, '%'))
             )
           ORDER BY c.area ASC, c.name ASC
           """)
    List<ServiceCentre> search(@Param("q") String query);

    List<ServiceCentre> findByActiveTrueOrderByAreaAscNameAsc();
    List<ServiceCentre> findByActiveTrueAndAreaIgnoreCaseContainingOrActiveTrueAndAddressIgnoreCaseContaining(
            String areaPart1, String areaPart2
    );
    @Query("select distinct c.area from ServiceCentre c where c.active = true order by c.area")
    List<String> findDistinctAreas();
}


