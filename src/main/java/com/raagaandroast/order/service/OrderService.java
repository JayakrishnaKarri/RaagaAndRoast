package com.raagaandroast.order.service;

import com.raagaandroast.cart.service.CartService;
import com.raagaandroast.common.exception.*;
import com.raagaandroast.customer.entity.Address;
import com.raagaandroast.customer.entity.Customer;
import com.raagaandroast.customer.repository.AddressRepository;
import com.raagaandroast.customer.repository.CustomerRepository;
import com.raagaandroast.menu.entity.MenuItem;
import com.raagaandroast.menu.repository.MenuItemRepository;
import com.raagaandroast.order.dto.CreateOrderRequest;
import com.raagaandroast.order.dto.OrderResponse;
import com.raagaandroast.order.dto.UpdateOrderStatusRequest;
import com.raagaandroast.order.entity.Order;
import com.raagaandroast.order.entity.OrderItem;
import com.raagaandroast.order.entity.OrderStatus;
import com.raagaandroast.order.mapper.OrderMapper;
import com.raagaandroast.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service responsible for order lifecycle and business logic.
 *
 * Responsibilities:
 * - Create orders
 * - Validate order data
 * - Calculate order totals
 * - Preserve menu-item price snapshots
 * - Manage order status transitions
 * - Manage order timestamps
 * - Validate customer ownership
 * - Provide order queries and reporting
 *
 * Transaction strategy:
 * - Read operations use read-only transactions.
 * - Write operations use read-write transactions.
 *
 * Concurrency:
 * - Order entity should use @Version for optimistic locking.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderService {

        private static final BigDecimal TAX_RATE = new BigDecimal("0.085");
        private static final BigDecimal DELIVERY_FEE = new BigDecimal("5.99");
        private static final int MONEY_SCALE = 2;

        private final OrderRepository orderRepository;
        private final CustomerRepository customerRepository;
        private final AddressRepository addressRepository;
        private final MenuItemRepository menuItemRepository;
        private final CartService cartService;
        private final OrderMapper orderMapper;

        // ================================================================
        // Order Creation
        // ================================================================

        /**
         * Creates a new order.
         *
         * Business flow:
         *
         * 1. Validate customer
         * 2. Validate delivery address
         * 3. Create order
         * 4. Validate menu items
         * 5. Capture price snapshots
         * 6. Calculate totals
         * 7. Persist order
         * 8. Clear customer cart
         *
         * @param customerId customer ID
         * @param request    order creation request
         * @return created order
         */
        @Transactional
        public OrderResponse createOrder(
                        UUID customerId,
                        CreateOrderRequest request) {

                log.info(
                                "Creating order for customer: {} with {} items",
                                customerId,
                                request.getItems().size());

                // 1. Validate customer
                Customer customer = findCustomer(customerId);

                // 2. Validate delivery address
                Address deliveryAddress = resolveDeliveryAddress(
                                customerId,
                                request.getDeliveryAddressId());

                // 3. Create order entity
                Order order = orderMapper.toOrderEntity(
                                request,
                                customer,
                                deliveryAddress);

                order.setStatus(OrderStatus.PENDING);

                // 4. Create order items and calculate subtotal
                BigDecimal subtotal = createOrderItems(
                                order,
                                request.getItems());

                // 5. Calculate totals
                calculateOrderTotals(order, subtotal);

                // 6. Persist order
                Order savedOrder = orderRepository.save(order);

                // 7. Clear cart after successful order creation
                //
                // This should participate in the same transaction if the
                // CartService uses the same database transaction.
                cartService.clearCart(customerId);

                log.info(
                                "Order created successfully. orderId={}, customerId={}, total={}",
                                savedOrder.getId(),
                                customerId,
                                savedOrder.getTotalAmount());

                return orderMapper.toOrderResponse(savedOrder);
        }

        /**
         * Creates order items and calculates subtotal.
         *
         * Important:
         * The client does NOT control the price.
         * The price is always taken from the current MenuItem.
         */
        private BigDecimal createOrderItems(
                        Order order,
                        List<CreateOrderRequest.OrderItemRequest> itemRequests) {

                List<OrderItem> orderItems = new ArrayList<>();

                BigDecimal subtotal = BigDecimal.ZERO;

                for (CreateOrderRequest.OrderItemRequest itemRequest : itemRequests) {

                        // Use JOIN FETCH to load category in single query (N+1 prevention)
                        MenuItem menuItem = menuItemRepository.findByIdWithCategory(
                                        itemRequest.getMenuItemId()).orElseThrow(
                                                        () -> new ResourceNotFoundException(
                                                                        "Menu item not found: "
                                                                                        + itemRequest.getMenuItemId()));

                        validateMenuItemAvailability(menuItem);

                        /*
                         * The mapper should copy the current menu-item price
                         * into OrderItem as a historical price snapshot.
                         */
                        OrderItem orderItem = orderMapper.toOrderItemEntity(
                                        itemRequest,
                                        order,
                                        menuItem);

                        orderItems.add(orderItem);

                        subtotal = subtotal.add(
                                        orderItem.getSubtotal());
                }

                order.setOrderItems(orderItems);

                return money(subtotal);
        }

        /**
         * Validates menu item availability.
         */
        private void validateMenuItemAvailability(MenuItem menuItem) {

                if (!Boolean.TRUE.equals(menuItem.getAvailable())) {
                        throw new MenuItemUnavailableException(
                                        String.format("Menu item '%s' is no longer available", menuItem.getName()));
                }
        }

        // ================================================================
        // Order Status Management
        // ================================================================

        /**
         * Updates order status.
         *
         * The Order entity is responsible for determining whether the
         * requested status transition is valid.
         */
        @Transactional
        public OrderResponse updateOrderStatus(
                        UUID orderId,
                        UpdateOrderStatusRequest request) {

                log.info(
                                "Updating order status. orderId={}, newStatus={}",
                                orderId,
                                request.getStatus());

                Order order = findOrder(orderId);

                return updateOrderStatus(order, request);
        }

        /**
         * Internal status update method.
         *
         * This method receives an already-loaded Order so callers such as
         * cancelOrder() do not need to query the same order twice.
         */
        private OrderResponse updateOrderStatus(
                        Order order,
                        UpdateOrderStatusRequest request) {

                OrderStatus oldStatus = order.getStatus();
                OrderStatus newStatus = request.getStatus();

                // Validate request
                validateStatusUpdateRequest(request);

                // Validate state transition
                validateStatusTransition(
                                oldStatus,
                                newStatus);

                // Update status
                order.setStatus(newStatus);

                // Update timestamps and status-specific fields
                updateStatusFields(
                                order,
                                newStatus,
                                request);

                // Save
                Order savedOrder = orderRepository.save(order);

                log.info(
                                "Order status updated. orderId={}, oldStatus={}, newStatus={}",
                                savedOrder.getId(),
                                oldStatus,
                                newStatus);

                return orderMapper.toOrderResponse(savedOrder);
        }

        /**
         * Validates whether the status transition is allowed.
         */
        private void validateStatusTransition(
                        OrderStatus currentStatus,
                        OrderStatus requestedStatus) {

                if (currentStatus == null) {
                        throw new BusinessRuleViolationException(
                                        "Order has no current status");
                }

                if (requestedStatus == null) {
                        throw new BusinessRuleViolationException(
                                        "Order status cannot be null");
                }

                if (!currentStatus.canTransitionTo(requestedStatus)) {
                        throw new InvalidOrderStatusTransitionException(
                                        String.format("Invalid order status transition from %s to %s",
                                                        currentStatus, requestedStatus));
                }
        }

        /**
         * Validates additional request fields based on status.
         */
        private void validateStatusUpdateRequest(
                        UpdateOrderStatusRequest request) {

                if (request == null) {
                        throw InvalidOrderStatusUpdateRequestException.nullRequest();
                }

                String validationError = request.validateRequest();

                if (validationError != null) {
                        throw InvalidOrderStatusUpdateRequestException.validationFailed(validationError);
                }
        }

        /**
         * Updates timestamp and status-specific fields.
         */
        private void updateStatusFields(
                        Order order,
                        OrderStatus newStatus,
                        UpdateOrderStatusRequest request) {

                LocalDateTime now = LocalDateTime.now();

                switch (newStatus) {

                        case PENDING -> {
                                // Initial state.
                                // No timestamp update required.
                        }

                        case CONFIRMED -> {
                                order.setConfirmedAt(now);
                        }

                        case PREPARING -> {
                                order.setPreparationStartedAt(now);
                        }

                        case READY -> {
                                order.setReadyAt(now);

                                if (request.getActualPrepTime() != null) {
                                        order.setActualPrepTime(
                                                        request.getActualPrepTime());
                                }
                        }

                        case COMPLETED -> {
                                order.setCompletedAt(now);

                                if (request.getActualPrepTime() != null) {
                                        order.setActualPrepTime(
                                                        request.getActualPrepTime());
                                }
                        }

                        case CANCELLED -> {
                                order.setCancelledAt(now);
                                order.setCancellationReason(
                                                request.getReason());
                        }
                }
        }

        // ================================================================
        // Order Cancellation
        // ================================================================

        /**
         * Cancels an order belonging to a specific customer.
         *
         * Ownership is validated before the order is modified.
         */
        @Transactional
        public OrderResponse cancelOrder(
                        UUID orderId,
                        UUID customerId,
                        String reason) {

                log.info(
                                "Cancelling order. orderId={}, customerId={}",
                                orderId,
                                customerId);

                Order order = orderRepository
                                .findByIdAndCustomerIdWithItems(
                                                orderId,
                                                customerId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Order not found: " + orderId));

                UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();

                request.setStatus(OrderStatus.CANCELLED);
                request.setReason(reason);

                return updateOrderStatus(
                                order,
                                request);
        }

        // ================================================================
        // Order Retrieval
        // ================================================================

        /**
         * Finds an order by ID.
         *
         * This method should only be exposed to authorized staff/admin
         * operations if arbitrary order access is allowed.
         */
        public OrderResponse findOrderById(UUID orderId) {

                Order order = orderRepository
                                .findByIdWithCompleteGraph(orderId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Order not found: " + orderId));

                return orderMapper.toOrderResponse(order);
        }

        /**
         * Finds an order belonging to a specific customer.
         *
         * This is the preferred method for customer-facing APIs.
         */
        public OrderResponse findOrderByIdAndCustomerId(
                        UUID orderId,
                        UUID customerId) {

                Order order = orderRepository
                                .findByIdAndCustomerIdWithItems(
                                                orderId,
                                                customerId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Order not found: " + orderId));

                return orderMapper.toOrderResponse(order);
        }

        /**
         * Gets customer orders using pagination.
         */
        public Page<OrderResponse> getCustomerOrders(
                        UUID customerId,
                        Pageable pageable) {

                // Use @EntityGraph for optimized loading with pagination
                Page<Order> orders = orderRepository
                                .findByCustomerId(
                                                customerId,
                                                pageable);

                return orders.map(
                                orderMapper::toOrderSummaryResponse);
        }

        /**
         * Gets orders by status.
         *
         * Intended for staff/admin operations.
         */
        public Page<OrderResponse> getOrdersByStatus(
                        OrderStatus status,
                        Pageable pageable) {

                // Note: This could be optimized with JOIN FETCH for order items
                // Currently using basic pagination - consider adding optimized version
                Page<Order> orders = orderRepository
                                .findByStatusOrderByCreatedAtDesc(
                                                status,
                                                pageable);

                return orders.map(
                                orderMapper::toOrderSummaryResponse);
        }

        /**
         * Gets orders waiting to be prepared.
         */
        public List<OrderResponse> getOrdersReadyForPreparation() {

                List<Order> orders = orderRepository.findOrdersReadyForPreparation();

                return orderMapper.toOrderResponseList(orders);
        }

        /**
         * Gets orders ready for delivery.
         */
        public List<OrderResponse> getOrdersReadyForDelivery() {

                List<Order> orders = orderRepository.findOrdersReadyForDelivery();

                return orderMapper.toOrderResponseList(orders);
        }

        /**
         * Gets overdue orders.
         */
        public List<OrderResponse> getOverdueOrders() {

                List<Order> orders = orderRepository.findOverdueOrders();

                return orderMapper.toOrderResponseList(orders);
        }

        // ================================================================
        // Business Logic
        // ================================================================

        /**
         * Calculates order totals.
         *
         * Formula:
         *
         * subtotal
         * + tax
         * + delivery fee
         * = total
         */
        private void calculateOrderTotals(
                        Order order,
                        BigDecimal subtotal) {

                BigDecimal normalizedSubtotal = money(subtotal);

                order.setSubtotal(
                                normalizedSubtotal);

                BigDecimal taxAmount = normalizedSubtotal
                                .multiply(TAX_RATE)
                                .setScale(
                                                MONEY_SCALE,
                                                RoundingMode.HALF_UP);

                order.setTaxAmount(
                                taxAmount);

                BigDecimal deliveryFee = order.getDeliveryAddress() != null
                                ? DELIVERY_FEE
                                : BigDecimal.ZERO;

                order.setDeliveryFee(
                                money(deliveryFee));

                BigDecimal totalAmount = normalizedSubtotal
                                .add(taxAmount)
                                .add(deliveryFee);

                order.setTotalAmount(
                                money(totalAmount));
        }

        /**
         * Normalizes monetary values to two decimal places.
         */
        private BigDecimal money(BigDecimal amount) {

                if (amount == null) {
                        return BigDecimal.ZERO.setScale(
                                        MONEY_SCALE,
                                        RoundingMode.HALF_UP);
                }

                return amount.setScale(
                                MONEY_SCALE,
                                RoundingMode.HALF_UP);
        }

        // ================================================================
        // Validation / Lookup Helpers
        // ================================================================

        /**
         * Finds a customer by ID.
         */
        private Customer findCustomer(UUID customerId) {

                // Use JOIN FETCH to load user relationship (N+1 prevention)
                return customerRepository
                                .findByIdWithAllRelationships(customerId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Customer not found: " + customerId));
        }

        /**
         * Resolves and validates the delivery address.
         */
        private Address resolveDeliveryAddress(
                        UUID customerId,
                        UUID deliveryAddressId) {

                if (deliveryAddressId == null) {
                        return null;
                }

                // Use JOIN FETCH to load customer relationship (N+1 prevention)
                Address address = addressRepository
                                .findByIdWithCustomer(deliveryAddressId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Address not found: "
                                                                + deliveryAddressId));

                if (address.getCustomer() == null
                                || address.getCustomer().getId() == null
                                || !address.getCustomer()
                                                .getId()
                                                .equals(customerId)) {

                        throw new AddressOwnershipException(
                                        "Delivery address does not belong to customer");
                }

                return address;
        }

        /**
         * Finds an order by ID.
         */
        private Order findOrder(UUID orderId) {

                return orderRepository
                                .findById(orderId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Order not found: " + orderId));
        }

        // ================================================================
        // Security / Ownership
        // ================================================================

        /**
         * Checks whether an order belongs to a customer.
         */
        public boolean isOrderOwnedByCustomer(
                        UUID orderId,
                        UUID customerId) {

                return orderRepository
                                .existsByIdAndCustomerId(
                                                orderId,
                                                customerId);
        }

        // ================================================================
        // Analytics / Reporting
        // ================================================================

        /**
         * Calculates total revenue for a date range.
         */
        public BigDecimal calculateRevenueForPeriod(
                        LocalDateTime startDate,
                        LocalDateTime endDate) {

                BigDecimal revenue = orderRepository.calculateRevenueForPeriod(
                                startDate,
                                endDate);

                return money(revenue);
        }

        /**
         * Gets order statistics grouped by status.
         */
        public List<Object[]> getOrderStatistics() {

                return orderRepository.countOrdersByStatus();
        }

        /**
         * Calculates average order value.
         */
        public BigDecimal calculateAverageOrderValue() {

                BigDecimal average = orderRepository.calculateAverageOrderValue();

                return money(average);
        }
}