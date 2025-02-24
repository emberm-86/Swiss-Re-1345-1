package org.swissre.assessment.service.discount;

import static java.util.Comparator.comparing;
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

public class Discount5thBevServiceImpl implements DiscountService {

  @Override
  public List<OrderItem> getDiscountedOrderItems(Integer orderId, OrderMap allOrders) {
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
    Comparator<Entry<Integer, MenuItem>> priceComparator = comparing(e -> e.getValue().getPrice());
    Comparator<Entry<Integer, MenuItem>> entryComparator =
        Entry.<Integer, MenuItem>comparingByKey().thenComparing(priceComparator.reversed());

    MenuItemList beverages =
        listOfMenuItemEntries.stream()
            .filter(menuItemEntry -> menuItemEntry.getValue().getType() == Type.BEVERAGE)
            .sorted(entryComparator)
            .collect(Collectors.toCollection(MenuItemList::new));

    return IntStream.range(0, beverages.size())
        .filter(i -> (i + 1) % 5 == 0)
        .mapToObj(beverages::get)
        .collect(Collectors.toCollection(MenuItemList::new));
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
}
