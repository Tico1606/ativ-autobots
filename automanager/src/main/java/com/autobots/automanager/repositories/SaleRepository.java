package com.autobots.automanager.repositories;

import com.autobots.automanager.entities.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleRepository extends JpaRepository<Sale, Long> {
}
