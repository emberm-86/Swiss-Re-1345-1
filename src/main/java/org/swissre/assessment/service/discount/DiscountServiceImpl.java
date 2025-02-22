package org.swissre.assessment.service.discount;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.summingInt;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.swissre.assessment.domain.MenuItem;
import org.swissre.assessment.domain.datastructure.MenuItemList;
import org.swissre.assessment.domain.OrderItem;
import org.swissre.assessment.domain.Type;
import org.swissre.assessment.domain.datastructure.OrderItemList;
import org.swissre.assessment.domain.datastructure.OrderMap;
import org.swissre.assessment.service.util.MenuItemConverter;

public class DiscountServiceImpl implements DiscountService {

  @Override
  public List<OrderItem> getDiscBev1Snack1(Integer orderId, OrderMap allOrders) {
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

  @Override
  public List<OrderItem> getDisOrdItems5thBev(Integer orderId, OrderMap allOrders) {
    OrderItemList listOfOrderItemEntries = convertOrdersToOrderItemList(allOrders);
    MenuItemList listOfMenuItemEntries = splitOrderItemListToMenuItemList(listOfOrderItemEntries);
    MenuItemList discountedMenuItems = filter5thBevDiscounts(listOfMenuItemEntries);
    Map<Integer, List<OrderItem>> discountedOrderMap = convertToOrderMap(discountedMenuItems);

    return discountedOrderMap.getOrDefault(orderId, new ArrayList<>());
  }

  private Map<Integer, List<OrderItem>> convertToOrderMap(MenuItemList discountedMenuItems) {
    return discountedMenuItems.stream()
        .collect(groupingBy(Entry::getKey, mapping(Entry::getValue, toList())))
        .entrySet()
        .stream()
        .collect(toMap(Entry::getKey, menuItE -> convertMenuItemsToOrderItems(menuItE.getValue())));
  }

  private List<OrderItem> convertMenuItemsToOrderItems(List<MenuItem> menuItems) {
    Map<String, Integer> menuItemOccurrences =
        menuItems.stream()
            .collect(groupingBy(MenuItem::getCode, LinkedHashMap::new, summingInt(e -> 1)));

    return menuItemOccurrences.entrySet().stream()
        .map(MenuItemConverter::convertToOrderItem)
        .toList();
  }

  private MenuItemList filter5thBevDiscounts(MenuItemList listOfMenuItemEntries) {
    List<SimpleEntry<Integer, MenuItem>> beverages =
        listOfMenuItemEntries.stream()
            .filter(menuItemEntry -> menuItemEntry.getValue().getType() == Type.BEVERAGE)
            .toList();

    Map<Integer, List<MenuItem>> discountedItemMap =
        beverages.stream()
            .collect(groupingBy(Entry::getKey, mapping(Entry::getValue, toList())))
            .entrySet()
            .stream()
            .collect(
                Collectors.toMap(
                    Entry::getKey,
                    e ->
                        e.getValue().stream()
                            .sorted(Comparator.comparing(MenuItem::getPrice))
                            .collect(Collectors.toCollection(LinkedList::new))));

    List<Integer> discountedOrderIds =
        IntStream.range(0, beverages.size())
            .filter(i -> (i + 1) % 5 == 0)
            .mapToObj(i -> beverages.get(i).getKey())
            .collect(Collectors.toCollection(LinkedList::new));

    MenuItemList menuItemList = new MenuItemList();

    // Selected the cheapest beverage for 5th beverage discount in every order to provide fairness.

    while (!discountedOrderIds.isEmpty()) {
      Integer discountedOrderId = discountedOrderIds.removeFirst();
      List<MenuItem> menuItems = discountedItemMap.get(discountedOrderId);
      MenuItem discountedMenuItem = menuItems.removeFirst();
      menuItemList.add(new SimpleEntry<>(discountedOrderId, discountedMenuItem));
    }
    return menuItemList;
  }

  private MenuItemList splitOrderItemListToMenuItemList(OrderItemList listOfOrderItemEntries) {
    return listOfOrderItemEntries.stream()
        .map(
            ordItE ->
                IntStream.range(0, ordItE.getValue().getQuantity())
                    .mapToObj(
                        i -> new SimpleEntry<>(ordItE.getKey(), ordItE.getValue().getMenuItem()))
                    .toList())
        .flatMap(Collection::stream)
        .collect(Collectors.toCollection(MenuItemList::new));
  }

  private OrderItemList convertOrdersToOrderItemList(OrderMap allOrders) {
    return allOrders.entrySet().stream()
        .flatMap(e -> e.getValue().stream().map(v -> new SimpleEntry<>(e.getKey(), v)))
        .collect(Collectors.toCollection(OrderItemList::new));
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
