package com.example.store.repository;

import com.example.store.entity.Customer;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @NonNull @Override
    @EntityGraph(attributePaths = "orders")
    List<Customer> findAll();

    @NonNull @EntityGraph(attributePaths = "orders")
    List<Customer> findByNameContainingIgnoreCase(String searchText);
}
