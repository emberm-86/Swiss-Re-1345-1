package org.swissre.assessment.service.billing;

import java.util.List;
import java.util.Optional;
import org.swissre.assessment.domain.OrderItem;

public class BillingServiceImpl implements BillingService {

  @Override
  public float calculateSum(List<OrderItem> order) {
    return order.stream().map(orderItem ->
            orderItem.getMenuItem().getPrice() * orderItem.getQuantity())
        .reduce(0f, Float::sum);
  }

  @Override
  public float calculateSumWithDiscounts(List<OrderItem> order,
      List<OrderItem> discountedOrderItems) {
    return order.stream().map(orderItem -> {
      float basePrice = orderItem.getMenuItem().getPrice() * orderItem.getQuantity();

      Optional<OrderItem> discounted = findInDiscountedList(discountedOrderItems, orderItem);

      return discounted.map(item -> basePrice - item.getQuantity() * item.getMenuItem().getPrice())
          .orElse(basePrice);
    }).reduce(0f, Float::sum);
  }

  private Optional<OrderItem> findInDiscountedList(List<OrderItem> discountedOrderItems,
      OrderItem orderItem) {
    return discountedOrderItems.stream().
        filter(discountedOrderItem -> discountedOrderItem.getMenuItem().getCode()
            .equals(orderItem.getMenuItem().getCode())).findFirst();
  }
}
