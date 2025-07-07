package org.swissre.assessment.domain;

public class OrderItem {

  private final MenuItem menuItem;
  private final int quantity;

  public OrderItem(MenuItem menuItem, int quantity) {
    this.menuItem = menuItem;
    this.quantity = quantity;
  }

  public MenuItem getMenuItem() {
    return menuItem;
  }

  public int getQuantity() {
    return quantity;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;

    OrderItem orderItem = (OrderItem) o;
    return quantity == orderItem.quantity && menuItem == orderItem.menuItem;
  }

  @Override
  public int hashCode() {
    int result = menuItem.hashCode();
    result = 31 * result + quantity;
    return result;
  }
}
