package org.swissre.assessment.service.billing;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.swissre.assessment.domain.OrderItem;

public class BillingServiceImpl implements BillingService {

  @Override
  public BigDecimal calcSum(List<OrderItem> order) {
    return order.stream().map(orderItem ->
      orderItem.getMenuItem().getPrice().multiply(
        new BigDecimal(String.valueOf(orderItem.getQuantity()))))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
  @Override
  public BigDecimal calcSumWithDisc(List<OrderItem> order,
      List<OrderItem> discountedOrderItems) {
    return normalizedOrder(order).stream().map(orderItem -> {
      BigDecimal basePrice = orderItem.getMenuItem().getPrice().multiply(
          new BigDecimal(String.valueOf(orderItem.getQuantity())));

      Optional<OrderItem> discounted = findInDiscountedList(discountedOrderItems, orderItem);
      return discounted.map(
              item -> basePrice.subtract(item.getMenuItem().getPrice().multiply(
                  new BigDecimal(String.valueOf(item.getQuantity())))))
          .orElse(basePrice);
    }).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private Optional<OrderItem> findInDiscountedList(List<OrderItem> discountedOrderItems,
      OrderItem orderItem) {
    return discountedOrderItems.stream().
        filter(discountedOrderItem -> discountedOrderItem.getMenuItem().getCode()
            .equals(orderItem.getMenuItem().getCode())).findFirst();
  }

  private List<OrderItem> normalizedOrder(List<OrderItem> orders) {
    return new ArrayList<>(
        orders.stream().collect(Collectors.toMap(OrderItem::getMenuItem, Function.identity(),
                (o1, o2) -> {
                  o1.setQuantity(o1.getQuantity() + o2.getQuantity());
                  return o1;
                }, LinkedHashMap::new))
            .values());
  }
}
