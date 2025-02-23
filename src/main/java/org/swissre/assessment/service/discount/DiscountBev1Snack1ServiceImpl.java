package org.swissre.assessment.service.discount;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingInt;

import java.util.*;
import java.util.stream.IntStream;
import org.swissre.assessment.domain.MenuItem;
import org.swissre.assessment.domain.OrderItem;
import org.swissre.assessment.domain.Type;
import org.swissre.assessment.domain.datastructure.OrderMap;
import org.swissre.assessment.service.util.MenuItemConverter;

public class DiscountBev1Snack1ServiceImpl implements DiscountService {

  @Override
  public List<OrderItem> getDiscountedOrderItems(Integer orderId, OrderMap allOrders) {
    List<OrderItem> order = allOrders.getOrDefault(orderId, new ArrayList<>());
    int maxGiftCount = maxGiftCount(order);
    List<MenuItem> flattedOrderList = flattenAnOrder(order);

    List<MenuItem> discountedExtraMenuItems =
        flattedOrderList.stream()
            .filter(menuItem -> menuItem.getType() == Type.EXTRA)
            .sorted(Comparator.comparing(MenuItem::getPrice).reversed())
            .limit(maxGiftCount)
            .toList();

    return convertMenuItemsToOrderItems(discountedExtraMenuItems);
  }

  private int maxGiftCount(List<OrderItem> order) {
    return Math.min(sumByType(order, Type.BEVERAGE), sumByType(order, Type.SNACK));
  }

  private int sumByType(List<OrderItem> order, Type snack) {
    return order.stream()
        .filter(orderItem -> orderItem.getMenuItem().getType() == snack)
        .map(OrderItem::getQuantity)
        .reduce(0, Integer::sum);
  }

  private List<OrderItem> convertMenuItemsToOrderItems(List<MenuItem> menuItems) {
    Map<String, Integer> menuItemOccurrences =
        menuItems.stream()
            .collect(groupingBy(MenuItem::getCode, LinkedHashMap::new, summingInt(e -> 1)));

    return menuItemOccurrences.entrySet().stream()
        .map(MenuItemConverter::convertToOrderItem)
        .toList();
  }

  private List<MenuItem> flattenAnOrder(List<OrderItem> order) {
    return order.stream()
        .map(
            orderItem ->
                IntStream.range(0, orderItem.getQuantity())
                    .mapToObj(i -> orderItem.getMenuItem())
                    .toList())
        .flatMap(Collection::stream)
        .toList();
  }
}
