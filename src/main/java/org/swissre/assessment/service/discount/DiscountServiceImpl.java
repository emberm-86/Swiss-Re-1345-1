package org.swissre.assessment.service.discount;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.summingInt;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
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
            .sorted(Comparator.comparing(MenuItem::getPrice))
            .limit(maxGiftCount)
            .collect(toList());

    return convertMenuItemsToOrderItems(discountedExtraMenuItems);
  }

  private int maxGiftCount(List<OrderItem> order) {
    return Math.min(sumByType(order, Type.BEVERAGE), sumByType(order, Type.SNACK));
  }

  private Integer sumByType(List<OrderItem> order, Type snack) {
    return order.stream()
        .filter(orderItem -> orderItem.getMenuItem().getType() == snack)
        .map(OrderItem::getQuantity)
        .reduce(0, Integer::sum);
  }

  @Override
  public List<OrderItem> getDisOrdItems5thBev(Integer orderId, OrderMap allOrders) {
    OrderItemList extractedOrders = extractOrders(allOrders);
    MenuItemList extractedOrdersWithReps = splitOrders(extractedOrders);
    MenuItemList discountedMenuItemsFromExtractedOrders = filterDiscounts(extractedOrdersWithReps);

    Map<Integer, List<OrderItem>> discountedOrdersMap =
        convertBack(discountedMenuItemsFromExtractedOrders);

    return discountedOrdersMap.getOrDefault(orderId, new ArrayList<>());
  }

  private Map<Integer, List<OrderItem>> convertBack(MenuItemList discMenuItemsFromExtOrders) {
    return discMenuItemsFromExtOrders.stream()
        .collect(groupingBy(Entry::getKey, mapping(Entry::getValue, toList())))
        .entrySet()
        .stream()
        .collect(
            toMap(
                Entry::getKey,
                menuItemListEntry -> convertMenuItemsToOrderItems(menuItemListEntry.getValue())));
  }

  private List<OrderItem> convertMenuItemsToOrderItems(List<MenuItem> menuItems) {
    Map<String, Integer> menuItemOccurrences =
        menuItems.stream()
            .collect(groupingBy(MenuItem::getCode, LinkedHashMap::new, summingInt(e -> 1)));

    return menuItemOccurrences.entrySet().stream()
        .map(MenuItemConverter::convertToOrderItem)
        .collect(toList());
  }

  private MenuItemList filterDiscounts(MenuItemList extOrdersWithReps) {
    List<SimpleEntry<Integer, MenuItem>> beverages =
        extOrdersWithReps.stream()
            .filter(menuItemEntry -> menuItemEntry.getValue().getType() == Type.BEVERAGE)
            .collect(toList());

    return IntStream.range(0, beverages.size())
        .mapToObj(i -> (i + 1) % 5 == 0 ? beverages.get(i) : null)
        .filter(Objects::nonNull)
        .collect(Collectors.toCollection(MenuItemList::new));
  }

  private MenuItemList splitOrders(OrderItemList extractedOrders) {
    return extractedOrders.stream()
        .map(
            extOrd ->
                IntStream.range(0, extOrd.getValue().getQuantity())
                    .mapToObj(
                        i -> new SimpleEntry<>(extOrd.getKey(), extOrd.getValue().getMenuItem()))
                    .collect(toList()))
        .flatMap(Collection::stream)
        .collect(Collectors.toCollection(MenuItemList::new));
  }

  private OrderItemList extractOrders(OrderMap allOrders) {
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
                    .collect(toList()))
        .flatMap(Collection::stream)
        .collect(toList());
  }
}
