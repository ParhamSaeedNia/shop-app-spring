package org.example.shopapp.auth.repository;

import org.example.shopapp.common.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {
    
    Optional<Otp> findByPhoneNumberAndOtpCodeAndIsUsedFalse(String phoneNumber, String otpCode);
    
    @Query("SELECT o FROM Otp o WHERE o.phoneNumber = :phoneNumber AND o.isUsed = false AND o.expiresAt > :now ORDER BY o.createdAt DESC")
    List<Otp> findValidOtpsByPhoneNumber(@Param("phoneNumber") String phoneNumber, @Param("now") LocalDateTime now);
    
    @Query("SELECT o FROM Otp o WHERE o.phoneNumber = :phoneNumber AND o.isUsed = false ORDER BY o.createdAt DESC")
    List<Otp> findUnusedOtpsByPhoneNumber(@Param("phoneNumber") String phoneNumber);
    
    @Query("SELECT COUNT(o) FROM Otp o WHERE o.phoneNumber = :phoneNumber AND o.createdAt > :since")
    long countByPhoneNumberSince(@Param("phoneNumber") String phoneNumber, @Param("since") LocalDateTime since);
    
    void deleteByPhoneNumberAndIsUsedTrue(String phoneNumber);
    
    @Query("DELETE FROM Otp o WHERE o.expiresAt < :now")
    void deleteExpiredOtps(@Param("now") LocalDateTime now);
}
