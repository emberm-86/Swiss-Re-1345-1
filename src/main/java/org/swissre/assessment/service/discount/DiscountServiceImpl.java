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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.IntStream;
import org.swissre.assessment.domain.MenuItem;
import org.swissre.assessment.domain.OrderItem;
import org.swissre.assessment.domain.Type;

public class DiscountServiceImpl implements DiscountService {

  @Override
  public List<OrderItem> getDiscBev1Snack1(Integer orderId,
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

    List<SimpleEntry<Integer, OrderItem>> extOrds = extractOrders(allOrders);
    List<SimpleEntry<Integer, MenuItem>> extOrdsWithReps = splitOrders(extOrds);
    List<SimpleEntry<Integer, MenuItem>> extOrdsDisc = filterDiscounts(extOrdsWithReps);

    Map<Integer, List<OrderItem>> discountedOrdersMap = convertBack(extOrdsDisc);

    return discountedOrdersMap.getOrDefault(orderId, new ArrayList<>());
  }

  private Map<Integer, List<OrderItem>> convertBack(
      List<SimpleEntry<Integer, MenuItem>> extOrdsDisc) {

    return extOrdsDisc.stream()
        .collect(groupingBy(Entry::getKey, mapping(Entry::getValue, toList())))
        .entrySet().stream().collect(toMap(Entry::getKey,
            menuItemEntry -> convertMenuItemsToOrderItems(menuItemEntry.getValue())));
  }

  private List<OrderItem> convertMenuItemsToOrderItems(List<MenuItem> menuItems) {
    Map<String, Integer> menuItemOccurrences = menuItems.stream()
        .collect(groupingBy(MenuItem::getCode, LinkedHashMap::new, summingInt(e -> 1)));

    return menuItemOccurrences.entrySet().stream()
        .map(menuItemOcc -> new OrderItem(MenuItem.getMenuItemByCode(menuItemOcc.getKey()),
            menuItemOcc.getValue()))
        .collect(toList());
  }

  private List<SimpleEntry<Integer, MenuItem>> filterDiscounts(
      List<SimpleEntry<Integer, MenuItem>> extOrdersWithReps) {

    List<SimpleEntry<Integer, MenuItem>> beverages = extOrdersWithReps.stream()
        .filter(menuItemEntry -> menuItemEntry.getValue().getType() == Type.BEVERAGE)
        .collect(toList());

    return IntStream.range(0, beverages.size())
        .mapToObj(i -> (i + 1) % 5 == 0 ? beverages.get(i) : null)
        .filter(Objects::nonNull).collect(toList());
  }

  private List<SimpleEntry<Integer, MenuItem>> splitOrders(
      List<SimpleEntry<Integer, OrderItem>> extractedOrders) {

    return extractedOrders.stream().map(ordItemInt ->
            IntStream.range(0, ordItemInt.getValue().getQuantity()).mapToObj(i ->
                    new SimpleEntry<>(ordItemInt.getKey(), ordItemInt.getValue().getMenuItem()))
                .collect(toList()))
        .flatMap(Collection::stream).collect(toList());
  }

  private List<SimpleEntry<Integer, OrderItem>> extractOrders(
      Map<Integer, List<OrderItem>> allOrders) {

    return allOrders.entrySet().stream()
        .flatMap(e -> e.getValue().stream().map(v -> new SimpleEntry<>(e.getKey(), v)))
        .collect(toList());
  }

  private List<MenuItem> flattenOrder(List<OrderItem> order) {
    return order.stream().map(orderItem ->
            IntStream.range(0, orderItem.getQuantity()).mapToObj(i -> orderItem.getMenuItem())
                .collect(toList()))
        .flatMap(Collection::stream).collect(toList());
  }
}
