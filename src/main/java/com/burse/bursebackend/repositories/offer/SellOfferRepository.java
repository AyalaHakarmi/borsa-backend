package com.burse.bursebackend.repositories.offer;

import com.burse.bursebackend.entities.offer.SellOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SellOfferRepository extends JpaRepository<SellOffer, String> {



}
