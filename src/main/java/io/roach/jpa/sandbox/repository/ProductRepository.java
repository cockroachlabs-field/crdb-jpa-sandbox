package io.roach.jpa.sandbox.repository;

import java.util.Optional;
import java.util.UUID;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.roach.jpa.sandbox.domain.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    @Lock(LockModeType.PESSIMISTIC_READ)
    Optional<Product> findProductBySku(String sku);

    @Query("select p from Product p where p.sku=:sku")
    Optional<Product> findProductBySkuNoLock(@Param("sku") String sku);

//    @Override
//    @Lock(LockModeType.PESSIMISTIC_READ)
//    List<Product> findAll();
}
