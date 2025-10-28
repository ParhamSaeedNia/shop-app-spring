package org.example.shopapp.order.repository;

import org.example.shopapp.common.entity.Shipment;
import org.example.shopapp.common.entity.Shipment.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    
    Optional<Shipment> findByOrderId(Long orderId);
    
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
    
    List<Shipment> findByStatus(ShipmentStatus status);
    
    @Query("SELECT s FROM Shipment s WHERE s.order.user.id = :userId")
    List<Shipment> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT s FROM Shipment s WHERE s.status = :status AND s.createdAt BETWEEN :startDate AND :endDate")
    List<Shipment> findByStatusAndDateRange(@Param("status") ShipmentStatus status, 
                                            @Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT s FROM Shipment s WHERE s.courier = :courier")
    List<Shipment> findByCourier(@Param("courier") String courier);
}
