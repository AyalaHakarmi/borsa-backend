package com.burse.bursebackend.repositories;

import com.burse.bursebackend.entities.Trader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TraderRepository extends JpaRepository<Trader, String> {
}

