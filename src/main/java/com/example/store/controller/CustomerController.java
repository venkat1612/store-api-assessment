package com.example.store.controller;

import com.example.store.dto.CustomerDTO;
import com.example.store.entity.Customer;
import com.example.store.mapper.CustomerMapper;
import com.example.store.repository.CustomerRepository;

import jakarta.persistence.EntityNotFoundException;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @GetMapping
    public List<CustomerDTO> getAllCustomers(@RequestParam(value = "name", required = false) String searchText) {
        List<Customer> customers;
        if (searchText != null && !searchText.isEmpty()) {
            customers = customerRepository.findByNameContainingIgnoreCase(searchText);

            if (customers.isEmpty()) {
                throw new EntityNotFoundException("No customers found matching name: " + searchText);
            }
        }
        return customerMapper.customersToCustomerDTOs(customerRepository.findAll());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerDTO createCustomer(@RequestBody Customer customer) {
        return customerMapper.customerToCustomerDTO(customerRepository.save(customer));
    }
}
