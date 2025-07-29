package com.burse.bursebackend.repositories.offer;

import com.burse.bursebackend.entities.offer.SellOffer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface SellOfferRepository extends JpaRepository<SellOffer, String> {

    @Query("""
    SELECT s FROM SellOffer s
    WHERE s.stock.id = :stockId
      AND s.price <= :maxPrice
    ORDER BY s.price ASC, s.createdAt ASC
    """)
    List<SellOffer> findBestMatchingSellOffer(
            @Param("stockId") String stockId,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);


}
