package com.restaurant.model.entity;

import com.restaurant.model.enums.TableStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(
    name = "restaurant_tables",
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_restaurant_tables_table_number", columnNames = "tableNumber")
    }
)
public class RestaurantTable extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @Min(1)
  @Max(999)
  @Column(nullable = false)
  private Integer tableNumber;

  @NotNull
  @Min(1)
  @Column(nullable = false)
  private Integer capacity = 4;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private TableStatus status = TableStatus.EMPTY;

  @Column(nullable = true, length = 200)
  private String position;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Integer getTableNumber() {
    return tableNumber;
  }

  public void setTableNumber(Integer tableNumber) {
    this.tableNumber = tableNumber;
  }

  public Integer getCapacity() {
    return capacity;
  }

  public void setCapacity(Integer capacity) {
    this.capacity = capacity;
  }

  public TableStatus getStatus() {
    return status;
  }

  public void setStatus(TableStatus status) {
    this.status = status;
  }

  public String getPosition() {
    return position;
  }

  public void setPosition(String position) {
    this.position = position;
  }
}

