package org.swissre.assessment.service.discount;

import static java.util.stream.Collectors.toList;
import static org.swissre.assessment.service.menu.MenuUtil.flattenOrder;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.swissre.assessment.domain.MenuItem;
import org.swissre.assessment.domain.OrderItem;
import org.swissre.assessment.domain.Type;

public class DiscountServiceImpl implements DiscountService{

  @Override
  public List<OrderItem> getDiscBevSnack1(Integer orderId,
      Map<Integer, List<OrderItem>> allOrders) {

    List<OrderItem> order = allOrders.getOrDefault(orderId, new ArrayList<>());
    int maxGiftCount = maxGiftCount(order);

    List<MenuItem> flattedOrderList = flattenOrder(order);

    List<MenuItem> extras = flattedOrderList.stream()
        .filter(menuItem -> menuItem.getType() == Type.EXTRA)
        .sorted(Comparator.comparing(MenuItem::getPrice).reversed())
        .collect(toList());

    Iterator<MenuItem> iterator = extras.iterator();

    List<MenuItem> discountedExtraMenuItems = new ArrayList<>();

    for (int i = 0; i < maxGiftCount && iterator.hasNext(); i++) {
      discountedExtraMenuItems.add(iterator.next());
    }

    return convertMenuItemsToOrderItems(discountedExtraMenuItems);
  }

  private int maxGiftCount(List<OrderItem> order) {
    return Math.min(sumByType(order, Type.BEVERAGE), sumByType(order, Type.SNACK));
  }

  private Integer sumByType(List<OrderItem> order, Type snack) {
    return order.stream()
        .filter(orderItem -> orderItem.getMenuItem().getType() == snack)
        .map(OrderItem::getQuantity).reduce(0, Integer::sum);
  }

  @Override
  public List<OrderItem> getDisOrdItems5thBev(Integer orderId,
      Map<Integer, List<OrderItem>> allOrders) {

    List<SimpleImmutableEntry<Integer, OrderItem>> extOrds = extractOrders(allOrders);
    List<SimpleImmutableEntry<Integer, MenuItem>> extOrdsWithDuplicates = splitOrders(extOrds);
    List<SimpleImmutableEntry<Integer, MenuItem>> extractedOrdersDiscounted = new ArrayList<>();

    filterDiscounts(extOrdsWithDuplicates, extractedOrdersDiscounted);

    Map<Integer, List<OrderItem>> discountedOrdersMap = convertBack(extractedOrdersDiscounted);

    return discountedOrdersMap.getOrDefault(orderId, new ArrayList<>());
  }

  private Map<Integer, List<OrderItem>> convertBack(
      List<SimpleImmutableEntry<Integer, MenuItem>> extOrdsDisc) {
    return extOrdsDisc.stream().collect(Collectors.groupingBy(Entry::getKey,
            Collectors.mapping(Entry::getValue, toList()))).entrySet().stream()
        .collect(Collectors.toMap(Entry::getKey, menuItemEntry -> {

          List<MenuItem> menuItems = menuItemEntry.getValue();
          return convertMenuItemsToOrderItems(menuItems);
        }));
  }

  private List<OrderItem> convertMenuItemsToOrderItems(List<MenuItem> menuItems) {
    Map<String, Integer> menuItemOccurrences = menuItems.stream()
        .collect(Collectors.groupingBy(
            MenuItem::getCode, LinkedHashMap::new, Collectors.summingInt(e -> 1)));

    return menuItemOccurrences.entrySet().stream()
        .map(menuItemOcc -> new OrderItem(MenuItem.getMenuItemByCode(menuItemOcc.getKey()),
            menuItemOcc.getValue()))
        .collect(toList());
  }

  private void filterDiscounts(
      List<SimpleImmutableEntry<Integer, MenuItem>> extractedOrdersWithDuplicates,
      List<SimpleImmutableEntry<Integer, MenuItem>> extractedOrdersDiscounted) {
    int i = 1;
    for (SimpleImmutableEntry<Integer, MenuItem> menuItemEntry : extractedOrdersWithDuplicates) {
      if (menuItemEntry.getValue().getType() == Type.BEVERAGE) {
        if (i % 5 == 0) {
          extractedOrdersDiscounted.add(menuItemEntry);
        }
        i++;
      }
    }
  }

  private List<SimpleImmutableEntry<Integer, MenuItem>> splitOrders(
      List<SimpleImmutableEntry<Integer, OrderItem>> extractedOrders) {
    List<SimpleImmutableEntry<Integer, MenuItem>> extractedOrdersWithDuplicates = new ArrayList<>();
    extractedOrders.forEach(order -> {
      int i = order.getValue().getQuantity();
      while (i > 0) {
        extractedOrdersWithDuplicates.add(
            new SimpleImmutableEntry<>(order.getKey(), order.getValue().getMenuItem()));
        i--;
      }
    });
    return extractedOrdersWithDuplicates;
  }

  private List<SimpleImmutableEntry<Integer, OrderItem>> extractOrders(
      Map<Integer, List<OrderItem>> allOrders) {
    return allOrders.entrySet().stream()
        .flatMap(e -> e.getValue().stream().map(v -> new SimpleImmutableEntry<>(e.getKey(), v)))
        .collect(toList());
  }
}
