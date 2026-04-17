package com.restaurant.model.entity;

import com.restaurant.model.enums.OrderStatus;
import com.restaurant.model.enums.OrderType;
import com.restaurant.model.enums.PaymentMethod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "orders")
public class Order extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = true)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "table_id", nullable = true)
  private RestaurantTable table;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private OrderType type;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private OrderStatus status = OrderStatus.PENDING;

  @NotNull
  @DecimalMin(value = "0.0", inclusive = true)
  @Digits(integer = 17, fraction = 2)
  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal totalPrice = BigDecimal.ZERO;

  @NotNull
  @DecimalMin(value = "0.0", inclusive = true)
  @Digits(integer = 17, fraction = 2)
  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal discountAmount = BigDecimal.ZERO;

  @NotNull
  @DecimalMin(value = "0.0", inclusive = true)
  @Digits(integer = 17, fraction = 2)
  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal finalPrice = BigDecimal.ZERO;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "voucher_id", nullable = true)
  private Voucher voucher;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PaymentMethod paymentMethod = PaymentMethod.CASH;

  @Column(nullable = true, length = 100)
  private String customerName;

  @Column(nullable = true, length = 11)
  private String customerPhone;

  @Column(nullable = true, length = 300)
  private String deliveryAddress;

  @Column(nullable = true, length = 500)
  private String customerNote;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public RestaurantTable getTable() {
    return table;
  }

  public void setTable(RestaurantTable table) {
    this.table = table;
  }

  public OrderType getType() {
    return type;
  }

  public void setType(OrderType type) {
    this.type = type;
  }

  public OrderStatus getStatus() {
    return status;
  }

  public void setStatus(OrderStatus status) {
    this.status = status;
  }

  public BigDecimal getTotalPrice() {
    return totalPrice;
  }

  public void setTotalPrice(BigDecimal totalPrice) {
    this.totalPrice = totalPrice;
  }

  public BigDecimal getDiscountAmount() {
    return discountAmount;
  }

  public void setDiscountAmount(BigDecimal discountAmount) {
    this.discountAmount = discountAmount;
  }

  public BigDecimal getFinalPrice() {
    return finalPrice;
  }

  public void setFinalPrice(BigDecimal finalPrice) {
    this.finalPrice = finalPrice;
  }

  public Voucher getVoucher() {
    return voucher;
  }

  public void setVoucher(Voucher voucher) {
    this.voucher = voucher;
  }

  public PaymentMethod getPaymentMethod() {
    return paymentMethod;
  }

  public void setPaymentMethod(PaymentMethod paymentMethod) {
    this.paymentMethod = paymentMethod;
  }

  public String getCustomerName() {
    return customerName;
  }

  public void setCustomerName(String customerName) {
    this.customerName = customerName;
  }

  public String getCustomerPhone() {
    return customerPhone;
  }

  public void setCustomerPhone(String customerPhone) {
    this.customerPhone = customerPhone;
  }

  public String getDeliveryAddress() {
    return deliveryAddress;
  }

  public void setDeliveryAddress(String deliveryAddress) {
    this.deliveryAddress = deliveryAddress;
  }

  public String getCustomerNote() {
    return customerNote;
  }

  public void setCustomerNote(String customerNote) {
    this.customerNote = customerNote;
  }
}

