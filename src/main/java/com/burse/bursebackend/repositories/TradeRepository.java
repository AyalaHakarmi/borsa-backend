package com.burse.bursebackend.repositories;

import com.burse.bursebackend.entities.Stock;
import com.burse.bursebackend.entities.Trade;
import com.burse.bursebackend.entities.Trader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, String> {
    List<Trade> findTop10ByStockOrderByTimestampDesc(Stock stock);
    List<Trade> findTop8ByBuyerOrSellerOrderByTimestampDesc(Trader buyer, Trader seller);

}
