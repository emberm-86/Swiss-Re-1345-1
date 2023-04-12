package org.swissre.assessment.service.order;

import static java.util.stream.Collectors.toList;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.swissre.assessment.domain.MenuItem;
import org.swissre.assessment.domain.OrderItem;
import org.swissre.assessment.domain.Type;
import org.swissre.assessment.service.billing.BillingService;
import org.swissre.assessment.service.billing.BillingServiceImpl;

public class OrderServiceImpl implements OrderService {

  List<OrderItem> orderSelectionCurrent = new ArrayList<>();
  OrderStorageProvider orderStorageProvider = new OrderStorageProviderImpl();
  BillingService billingService = new BillingServiceImpl();

  @Override
  public void addNewOrder(MenuItem menuItemSelected, String menuCode) {
    orderSelectionCurrent.add(new OrderItem(menuItemSelected, Integer.parseInt(menuCode)));
  }

  @Override
  public void closeOrder() {
    if (orderSelectionCurrent.isEmpty()) {
      return;
    }

    orderStorageProvider.addNewOrder(orderSelectionCurrent);

    System.out.println("You can check your bill here.");
    printSingleOrder(orderStorageProvider.getLastOrderIndex(), orderSelectionCurrent,
        orderStorageProvider.getAllOrders());
    orderSelectionCurrent.clear();
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
  public List<OrderItem> getDiscountsBeverage1Snack1(List<OrderItem> order) {
    int maxGiftCount = maxGiftCount(order);

    List<MenuItem> flattedOrderList = new ArrayList<>();
    for (OrderItem orderItem : order) {
      for (int k = orderItem.getQuantity(); k > 0; k--) {
        flattedOrderList.add(orderItem.getMenuItem());
      }
    }

    List<MenuItem> extras = flattedOrderList.stream()
        .filter(menuItem -> menuItem.getType() == Type.EXTRA)
        .sorted((m1, m2) -> Float.compare(m1.getPrice(), m2.getPrice()))
        .collect(toList());

    Iterator<MenuItem> iterator = extras.iterator();

    List<MenuItem> discountedExtraMenuItems = new ArrayList<>();

    for (int i = 0; i < maxGiftCount && iterator.hasNext(); i++) {
      discountedExtraMenuItems.add(iterator.next());
    }

    return convertMenuItemsToOrderItems(discountedExtraMenuItems);
  }

  @Override
  public void printAllOrders() {
    System.out.println("========================");

    Map<Integer, List<OrderItem>> allOrders = orderStorageProvider.getAllOrders();
    if (allOrders.isEmpty()) {
      System.out.println("There is no order in the system.");
    }

    allOrders.forEach((orderId, order) -> {
      if (orderId > 0) {
        System.out.println();
      }

      System.out.println("Order: " + orderId);
      printSingleOrder(orderId, order, allOrders);
    });
    System.out.println("========================\n");
  }

  public void printSingleOrder(Integer orderId, List<OrderItem> order,
      Map<Integer, List<OrderItem>> allOrders) {
    System.out.println("-------------------------------------------");
    prettyPrintOrder(order);
    System.out.println("-------------------------------------------");

    float billForOrder = billingService.calculateSum(order);
    int baseShift = 32 + maxQuantityAndSumPriceStrLength(order);
    int shift = baseShift - String.format("%.02f", billForOrder).length();

    System.out.printf("%-" + shift + "s %.02f %s %n", "Total:", billForOrder, "CHF");

    List<OrderItem> discountedOrderItems5thBeverage = getDiscountedOrders5thBeverage(allOrders)
        .getOrDefault(orderId, new ArrayList<>());

    List<OrderItem> discountedOrderItemsBeverage1Snack1 = getDiscountsBeverage1Snack1(order);

    List<OrderItem> discountedOrderItems = Stream.concat(discountedOrderItems5thBeverage.stream(),
            discountedOrderItemsBeverage1Snack1.stream())
        .collect(toList());

    for (int i = 0; i < discountedOrderItems.size(); i++) {

      OrderItem discountedOrderItem = discountedOrderItems.get(i);
      float discount = discountedOrderItem.getMenuItem().getPrice();
      int discountShift = baseShift - String.format("%.02f", discount).length();

      String discountType =
          i < discountedOrderItems5thBeverage.size() ? "beverage5th" : "beverage1snack1";

      System.out.printf("%-" + discountShift + "s%s%.02f %s", discountType, "-", discount, "CHF");

      if (i < discountedOrderItems.size() - 1) {
        System.out.println();
      }
    }

    if (!discountedOrderItems.isEmpty()) {
      System.out.println();
    }
    System.out.println("-------------------------------------------");

    float billForOrderDisc = billingService.calculateSumWithDiscounts(order, discountedOrderItems);

    int discountSumShift = baseShift - String.format("%.02f", billForOrderDisc).length();

    System.out.printf("%-" + discountSumShift + "s %.02f %s %n", "Total with discount:",
        billForOrderDisc, "CHF");
  }

  @Override
  public Map<Integer, List<OrderItem>> getDiscountedOrders5thBeverage(
      Map<Integer, List<OrderItem>> allOrders) {
    List<SimpleImmutableEntry<Integer, OrderItem>> extractedOrders = extractOrders(allOrders);

    List<SimpleImmutableEntry<Integer, MenuItem>> extractedOrdersWithDuplicates = splitOrders(
        extractedOrders);

    List<SimpleImmutableEntry<Integer, MenuItem>> extractedOrdersDiscounted = new ArrayList<>();

    filterDiscounts(extractedOrdersWithDuplicates, extractedOrdersDiscounted);

    return convertBack(extractedOrdersDiscounted);
  }

  private Map<Integer, List<OrderItem>> convertBack(
      List<SimpleImmutableEntry<Integer, MenuItem>> extractedOrdersDiscounted) {
    return extractedOrdersDiscounted.stream().collect(
            Collectors.groupingBy(Entry::getKey, Collectors.mapping(Entry::getValue, toList())))
        .entrySet().stream().collect(Collectors.toMap(Entry::getKey, menuItemEntry -> {
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

  private void prettyPrintOrder(List<OrderItem> orderItems) {
    int maxQuantityStrLength = maxQuantityStrLength(orderItems);
    int maxSumPriceStrLength = maxSumPriceStrLength(orderItems);

    orderItems.stream().map(orderItem ->
            printOrder(orderItem, maxQuantityStrLength, maxSumPriceStrLength))
        .forEach(System.out::println);
  }

  private String printOrder(OrderItem orderItem, int maxQuantityLength, int maxSumPriceStrLength) {
    MenuItem menuItem = orderItem.getMenuItem();
    float sumPrice = orderItem.getQuantity() * menuItem.getPrice();

    int shiftQuantity = maxQuantityLength - String.valueOf(orderItem.getQuantity()).length();
    int shiftSumPrice = maxSumPriceStrLength - String.format("%.02f", sumPrice).length();
    int shift = shiftQuantity + shiftSumPrice;

    String spaceBeforePrice = shift > 0 ? "%-" + shift + "s" : "%s";

    return String.format("%-14s %-8s %.02f %s %d " + spaceBeforePrice + " %.02f %s",
        menuItem.getName(), " (" + menuItem.getCode() + ")",
        menuItem.getPrice(), "X", orderItem.getQuantity(), "", sumPrice, "CHF");
  }

  private int maxQuantityStrLength(List<OrderItem> orders) {
    return orders.stream().map(OrderItem::getQuantity).map(String::valueOf).map(String::length)
        .max(Integer::compareTo).orElse(0);
  }

  private int maxSumPriceStrLength(List<OrderItem> orders) {
    return orders.stream()
        .map(orderItem -> orderItem.getQuantity() * orderItem.getMenuItem().getPrice())
        .map(sumPrice -> String.format("%.02f", sumPrice)).map(String::length)
        .max(Integer::compareTo).orElse(0);
  }

  private int maxQuantityAndSumPriceStrLength(List<OrderItem> orders) {
    return orders.stream().map(orderItem -> {
          float sumPrice = orderItem.getQuantity() * orderItem.getMenuItem().getPrice();
          return String.format("%.02f", sumPrice).length() + String.valueOf(orderItem.getQuantity())
              .length();
        })
        .max(Integer::compareTo).orElse(0);
  }
}
