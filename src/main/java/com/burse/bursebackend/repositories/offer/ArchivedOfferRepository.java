package com.burse.bursebackend.repositories.offer;

import com.burse.bursebackend.entities.offer.ArchivedOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ArchivedOfferRepository extends JpaRepository<ArchivedOffer, String> {

    @Query("SELECT o FROM ArchivedOffer o WHERE o.stock.id = :stockId AND o.archivedAt >= :after")
    List<ArchivedOffer> findByStockIdAndTimestampAfter(@Param("stockId") String stockId,
                                                       @Param("after") LocalDateTime after);

}
