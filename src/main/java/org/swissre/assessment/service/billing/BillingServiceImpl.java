package org.swissre.assessment.service.billing;

import static java.util.stream.Collectors.summarizingInt;
import static java.util.stream.Collectors.summingInt;
import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.swissre.assessment.domain.MenuItem;
import org.swissre.assessment.domain.OrderItem;

public class BillingServiceImpl implements BillingService {

  @Override
  public BigDecimal calcSum(List<OrderItem> order) {
    return order.stream().map(orderItem ->
      orderItem.getMenuItem().getPrice().multiply(
        new BigDecimal(String.valueOf(orderItem.getQuantity()))))
        .reduce(new BigDecimal("0.00"), BigDecimal::add);
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
    }).reduce(new BigDecimal("0.00"), BigDecimal::add);
  }

  private Optional<OrderItem> findInDiscountedList(List<OrderItem> discountedOrderItems,
      OrderItem orderItem) {
    return discountedOrderItems.stream().
        filter(discountedOrderItem -> discountedOrderItem.getMenuItem().getCode()
            .equals(orderItem.getMenuItem().getCode())).findFirst();
  }

  private List<OrderItem> normalizedOrder(List<OrderItem> orders) {
    Map<String, Integer> menuItems = orders.stream()
        .collect(Collectors.groupingBy(orderItem -> orderItem.getMenuItem().getCode(),
            LinkedHashMap::new, summingInt(OrderItem::getQuantity)));

    return menuItems.entrySet().stream()
        .map(menuItemOcc -> new OrderItem(MenuItem.getMenuItemByCode(menuItemOcc.getKey()),
            menuItemOcc.getValue()))
        .collect(toList());
  }
}
