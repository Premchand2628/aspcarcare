package com.carwash.membership.repository;

import com.carwash.membership.entity.Membership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

  boolean existsByMembershipId(String membershipId);

  Optional<Membership> findByMembershipId(String membershipId);

  Optional<Membership> findTopByPhoneOrderByUpdatedAtDesc(String phone);

  Optional<Membership> findTopByPhoneAndStatusOrderByUpdatedAtDesc(String phone, String status);
  
  List<Membership> findAllByPhone(String phone);
}
//package com.carwash.membership.repository;
//
//import com.carwash.membership.entity.Membership;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.Optional;
//
//public interface MembershipRepository extends JpaRepository<Membership, Long> {
//
//  boolean existsByMembershipId(String membershipId);
//
//  Optional<Membership> findByMembershipId(String membershipId);
//
//  Optional<Membership> findTopByPhoneOrderByUpdatedAtDesc(String phone);
//
//  Optional<Membership> findTopByPhoneAndStatus(String phone, String status);
//}
