package com.raagaandroast.customer.mapper;

import com.raagaandroast.customer.dto.*;
import com.raagaandroast.customer.entity.Address;
import com.raagaandroast.customer.entity.Customer;
import com.raagaandroast.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper class for converting between Customer entities and DTOs.
 * 
 * This mapper handles the conversion between Customer entities and their
 * corresponding DTOs, ensuring proper data transformation and security
 * by excluding sensitive information from responses.
 * 
 * Design Decisions:
 * - Manual mapping for full control over data transformation
 * - Separate methods for different mapping scenarios
 * - Security-conscious mapping (excludes sensitive data)
 * - Null-safe operations throughout
 * - Address mapping integration
 * 
 * Performance Considerations:
 * - Efficient stream operations for collections
 * - Lazy evaluation where possible
 * - Minimal object creation
 * 
 * Interview Points:
 * - Why manual mapping? Control, security, performance
 * - Why separate mapper class? Single Responsibility Principle
 * - Why null checks? Defensive programming, robustness
 * - How to handle relationships? Careful mapping to prevent N+1
 * 
 * @author RaagaAndRoast Development Team
 */
@Component
public class CustomerMapper {

    private final AddressMapper addressMapper;

    public CustomerMapper(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    // ================================================================
    // Entity to DTO Mapping
    // ================================================================

    /**
     * Converts a Customer entity to CustomerResponse DTO.
     * 
     * @param customer the customer entity
     * @return CustomerResponse DTO
     */
    public CustomerResponse toResponse(Customer customer) {
        if (customer == null) {
            return null;
        }

        User user = customer.getUser();
        List<AddressResponse> addressResponses = customer.getAddresses() != null
                ? customer.getAddresses().stream()
                        .map(addressMapper::toResponse)
                        .collect(Collectors.toList())
                : List.of();

        return new CustomerResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getPhoneNumber(),
                customer.getDateOfBirth(),
                customer.getPreferences(),
                customer.getMarketingConsent(),
                user != null ? user.getId() : null,
                user != null ? user.getUsername() : null,
                user != null ? user.getEmail() : null,
                user != null ? user.getEnabled() : null,
                addressResponses,
                customer.getCreatedAt(),
                customer.getUpdatedAt(),
                customer.getVersion());
    }

    /**
     * Converts a Customer entity to CustomerResponse DTO without addresses.
     * Useful for list views where address details are not needed.
     * 
     * @param customer the customer entity
     * @return CustomerResponse DTO without addresses
     */
    public CustomerResponse toResponseWithoutAddresses(Customer customer) {
        if (customer == null) {
            return null;
        }

        User user = customer.getUser();

        return new CustomerResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getPhoneNumber(),
                customer.getDateOfBirth(),
                customer.getPreferences(),
                customer.getMarketingConsent(),
                user != null ? user.getId() : null,
                user != null ? user.getUsername() : null,
                user != null ? user.getEmail() : null,
                user != null ? user.getEnabled() : null,
                List.of(), // Empty address list
                customer.getCreatedAt(),
                customer.getUpdatedAt(),
                customer.getVersion());
    }

    /**
     * Converts a list of Customer entities to CustomerResponse DTOs.
     * 
     * @param customers the list of customer entities
     * @return list of CustomerResponse DTOs
     */
    public List<CustomerResponse> toResponseList(List<Customer> customers) {
        if (customers == null) {
            return List.of();
        }

        return customers.stream()
                .map(this::toResponseWithoutAddresses) // Use without addresses for performance
                .collect(Collectors.toList());
    }

    // ================================================================
    // DTO to Entity Mapping
    // ================================================================

    /**
     * Converts CreateCustomerRequest DTO to Customer entity.
     * 
     * @param request the create customer request
     * @param user    the associated user entity
     * @return Customer entity
     */
    public Customer toEntity(CreateCustomerRequest request, User user) {
        if (request == null) {
            return null;
        }

        Customer customer = Customer.builder()
                .user(user)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phoneNumber(request.phoneNumber())
                .dateOfBirth(request.dateOfBirth())
                .preferences(request.preferences())
                .marketingConsent(request.getMarketingConsentOrDefault())
                .build();

        // Add address if provided
        if (request.hasAddressInfo()) {
            Address address = Address.builder()
                    .customer(customer)
                    .type(request.getAddressTypeOrDefault())
                    .streetAddress(request.streetAddress())
                    .addressLine2(request.addressLine2())
                    .city(request.city())
                    .state(request.state())
                    .postalCode(request.postalCode())
                    .country(request.getCountryOrDefault())
                    .deliveryInstructions(request.deliveryInstructions())
                    .isDefault(true) // First address is default
                    .isActive(true)
                    .build();

            customer.addAddress(address);
        }

        return customer;
    }

    /**
     * Updates an existing Customer entity with data from UpdateCustomerRequest.
     * Only updates fields that are provided in the request.
     * 
     * @param customer the existing customer entity
     * @param request  the update customer request
     */
    public void updateEntity(Customer customer, UpdateCustomerRequest request) {
        if (customer == null || request == null) {
            return;
        }

        if (request.firstName() != null) {
            customer.setFirstName(request.firstName());
        }

        if (request.lastName() != null) {
            customer.setLastName(request.lastName());
        }

        if (request.phoneNumber() != null) {
            customer.setPhoneNumber(request.phoneNumber());
        }

        if (request.dateOfBirth() != null) {
            customer.setDateOfBirth(request.dateOfBirth());
        }

        if (request.preferences() != null) {
            customer.setPreferences(request.preferences());
        }

        if (request.marketingConsent() != null) {
            customer.setMarketingConsent(request.marketingConsent());
        }

        // Version is handled by JPA optimistic locking
    }

    // ================================================================
    // Utility Methods
    // ================================================================

    /**
     * Creates a minimal CustomerResponse for summary views.
     * Contains only essential information.
     * 
     * @param customer the customer entity
     * @return minimal CustomerResponse DTO
     */
    public CustomerResponse toSummaryResponse(Customer customer) {
        if (customer == null) {
            return null;
        }

        User user = customer.getUser();

        return new CustomerResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                null, // No phone in summary
                null, // No date of birth in summary
                null, // No preferences in summary
                null, // No marketing consent in summary
                user != null ? user.getId() : null,
                user != null ? user.getUsername() : null,
                user != null ? user.getEmail() : null,
                user != null ? user.getEnabled() : null,
                List.of(), // No addresses in summary
                customer.getCreatedAt(),
                customer.getUpdatedAt(),
                customer.getVersion());
    }

    /**
     * Checks if a customer entity has all required fields for response mapping.
     * 
     * @param customer the customer entity
     * @return true if customer is valid for mapping
     */
    public boolean isValidForMapping(Customer customer) {
        return customer != null &&
                customer.getId() != null &&
                customer.getFirstName() != null &&
                customer.getLastName() != null &&
                customer.getUser() != null;
    }

    /**
     * Maps customer with specific address loading strategy.
     * Useful when you need to control address loading for performance.
     * 
     * @param customer         the customer entity
     * @param includeAddresses whether to include addresses
     * @return CustomerResponse DTO
     */
    public CustomerResponse toResponse(Customer customer, boolean includeAddresses) {
        if (includeAddresses) {
            return toResponse(customer);
        } else {
            return toResponseWithoutAddresses(customer);
        }
    }

    /**
     * Creates a CustomerResponse with only public information.
     * Excludes sensitive data like phone number and preferences.
     * 
     * @param customer the customer entity
     * @return public CustomerResponse DTO
     */
    public CustomerResponse toPublicResponse(Customer customer) {
        if (customer == null) {
            return null;
        }

        User user = customer.getUser();

        return new CustomerResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                null, // No phone number in public view
                null, // No date of birth in public view
                null, // No preferences in public view
                null, // No marketing consent in public view
                user != null ? user.getId() : null,
                user != null ? user.getUsername() : null,
                null, // No email in public view
                user != null ? user.getEnabled() : null,
                List.of(), // No addresses in public view
                customer.getCreatedAt(),
                null, // No update time in public view
                null // No version in public view
        );
    }
}