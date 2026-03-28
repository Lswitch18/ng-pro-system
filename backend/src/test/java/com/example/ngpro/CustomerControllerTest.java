package com.example.ngpro;

import com.example.ngpro.model.Customer;
import com.example.ngpro.repository.CustomerRepository;
import com.example.ngpro.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerRepository customerRepository;

    @MockBean
    private EmailService emailService;

    @Test
    void testGetAllCustomers() throws Exception {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("John Doe");
        customer.setEmail("john@example.com");
        customer.setStatus("ACTIVE");

        Page<Customer> page = new PageImpl<>(Arrays.asList(customer));
        when(customerRepository.findAll(any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("John Doe"))
                .andExpect(jsonPath("$.content[0].email").value("john@example.com"));
    }

    @Test
    void testGetCustomerById() throws Exception {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("John Doe");
        customer.setEmail("john@example.com");
        customer.setStatus("ACTIVE");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void testGetCustomerNotFound() throws Exception {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/customers/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateCustomer() throws Exception {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("John Doe");
        customer.setEmail("john@example.com");
        customer.setStatus("ACTIVE");

        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        doNothing().when(emailService).sendWelcomeEmail(anyString(), anyString());

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "John Doe",
                        "email": "john@example.com",
                        "phone": "11999999999",
                        "status": "ACTIVE"
                    }
                    """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void testCreateCustomerWithInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "John Doe",
                        "email": "invalid-email",
                        "phone": "11999999999",
                        "status": "ACTIVE"
                    }
                    """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateCustomerWithInvalidStatus() throws Exception {
        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "John Doe",
                        "email": "john@example.com",
                        "phone": "11999999999",
                        "status": "INVALID_STATUS"
                    }
                    """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateCustomer() throws Exception {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("John Doe");
        customer.setEmail("john@example.com");
        customer.setStatus("ACTIVE");

        Customer updatedCustomer = new Customer();
        updatedCustomer.setId(1L);
        updatedCustomer.setName("John Updated");
        updatedCustomer.setEmail("john.updated@example.com");
        updatedCustomer.setStatus("SUSPENDED");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(updatedCustomer);

        mockMvc.perform(put("/api/customers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "John Updated",
                        "email": "john.updated@example.com",
                        "phone": "11999999999",
                        "status": "SUSPENDED"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Updated"))
                .andExpect(jsonPath("$.status").value("SUSPENDED"));
    }

    @Test
    void testDeleteCustomer() throws Exception {
        when(customerRepository.existsById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/customers/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testSearchCustomers() throws Exception {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("John Doe");
        customer.setEmail("john@example.com");

        when(customerRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase("John", "John"))
                .thenReturn(Arrays.asList(customer));

        mockMvc.perform(get("/api/customers/search").param("q", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("John Doe"));
    }
}
