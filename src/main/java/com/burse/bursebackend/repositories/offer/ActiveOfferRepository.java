package com.burse.bursebackend.repositories.offer;

import com.burse.bursebackend.entities.offer.ActiveOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActiveOfferRepository extends JpaRepository<ActiveOffer, String> {

    @Query("SELECT o FROM ActiveOffer o WHERE o.stock.id = :stockId")
    List<ActiveOffer> findByStockId(@Param("stockId") String stockId);
}
