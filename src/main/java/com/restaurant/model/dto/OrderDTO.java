package com.restaurant.model.dto;

import com.restaurant.model.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class OrderDTO {
  @NotNull
  private PaymentMethod paymentMethod = PaymentMethod.CASH;

  @Size(max = 300)
  private String deliveryAddress;

  @Size(max = 100)
  private String customerName;

  @Size(max = 11)
  private String customerPhone;

  @Size(max = 500)
  private String customerNote;

  public PaymentMethod getPaymentMethod() {
    return paymentMethod;
  }

  public void setPaymentMethod(PaymentMethod paymentMethod) {
    this.paymentMethod = paymentMethod;
  }

  public String getDeliveryAddress() {
    return deliveryAddress;
  }

  public void setDeliveryAddress(String deliveryAddress) {
    this.deliveryAddress = deliveryAddress;
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

  public String getCustomerNote() {
    return customerNote;
  }

  public void setCustomerNote(String customerNote) {
    this.customerNote = customerNote;
  }
}

