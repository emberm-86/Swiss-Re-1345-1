package org.swissre.assessment.service.billing;

import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.swissre.assessment.domain.OrderItem;
import org.swissre.assessment.service.menu.MenuItemConverter;

public class BillingServiceImpl implements BillingService {

  @Override
  public BigDecimal calcSum(List<OrderItem> order) {
    return order.stream().map(BillingServiceImpl::getSumPrice).reduce(ZERO, BigDecimal::add);
  }

  @Override
  public BigDecimal calcSumWithDisc(List<OrderItem> order, List<OrderItem> discOrderItems) {
    return normalizedOrder(order).stream()
        .map(
            orderItem -> {
              BigDecimal sumPrice = getSumPrice(orderItem);
              Optional<OrderItem> discounted = findInDiscountedList(discOrderItems, orderItem);

              return discounted
                  .map(item -> calculateDiscountedPrice(item, sumPrice))
                  .orElse(sumPrice);
            })
        .reduce(ZERO, BigDecimal::add);
  }

  private static BigDecimal calculateDiscountedPrice(OrderItem item, BigDecimal sumPrice) {
    BigDecimal disPrice = item.getMenuItem().getPrice();
    BigDecimal disQuantity = new BigDecimal(String.valueOf(item.getQuantity()));
    return sumPrice.subtract(disPrice.multiply(disQuantity));
  }

  private Optional<OrderItem> findInDiscountedList(List<OrderItem> disOrdItems, OrderItem ordItem) {
    return disOrdItems.stream()
        .filter(
            disOrderItem ->
                disOrderItem.getMenuItem().getCode().equals(ordItem.getMenuItem().getCode()))
        .findFirst();
  }

  private List<OrderItem> normalizedOrder(List<OrderItem> orders) {
    Map<String, Integer> menuItems =
        orders.stream()
            .collect(
                Collectors.groupingBy(
                    orderItem -> orderItem.getMenuItem().getCode(),
                    LinkedHashMap::new,
                    Collectors.summingInt(OrderItem::getQuantity)));

    return menuItems.entrySet().stream()
        .map(MenuItemConverter::convertToOrderItem)
        .collect(Collectors.toList());
  }

  private static BigDecimal getSumPrice(OrderItem orderItem) {
    BigDecimal price = orderItem.getMenuItem().getPrice();
    BigDecimal quantity = new BigDecimal(String.valueOf(orderItem.getQuantity()));
    return price.multiply(quantity);
  }
}
