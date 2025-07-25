package com.burse.bursebackend.repositories.offer;

import com.burse.bursebackend.entities.offer.BuyOffer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BuyOfferRepository extends JpaRepository<BuyOffer, String> {
    @Query("""
    SELECT b FROM BuyOffer b
    WHERE b.stock.id = :stockId
      AND b.price >= :minPrice
    ORDER BY b.price DESC, b.createdAt ASC
    """)
    Optional<BuyOffer> findBestMatchingBuyOffer(
            @Param("stockId") String stockId,
            @Param("minPrice") BigDecimal minPrice);


    boolean existsByTraderIdAndStockId(String traderId, String stockId);


}
