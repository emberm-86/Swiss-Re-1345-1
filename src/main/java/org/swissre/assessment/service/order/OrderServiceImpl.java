package org.swissre.assessment.service.order;

import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Comparator;
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
      System.out.println("You have ordered nothing.");
      return;
    }

    orderStorageProvider.addNewOrder(new ArrayList<>(orderSelectionCurrent));

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

  public List<OrderItem> getDiscountsBeverage1Snack1(Integer orderId,
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

  private static List<MenuItem> flattenOrder(List<OrderItem> order) {
    List<MenuItem> flattedOrderList = new ArrayList<>();
    for (OrderItem orderItem : order) {
      for (int k = orderItem.getQuantity(); k > 0; k--) {
        flattedOrderList.add(orderItem.getMenuItem());
      }
    }
    return flattedOrderList;
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

    BigDecimal billForOrder = billingService.calcSum(order);
    int baseShift = 32 + maxQuantityAndSumPriceStrLength(order);
    int shift = baseShift - String.format("%.02f", billForOrder).length();

    System.out.printf("%-" + shift + "s %.02f %s %n", "Total:", billForOrder, "CHF");

    List<OrderItem> disOrderItems5thBeverage = getDisOrdItems5thBev(orderId, allOrders);
    List<OrderItem> disOrderItemsBev1Snack1 = getDiscountsBeverage1Snack1(orderId, allOrders);

    List<OrderItem> disOrderItems = Stream.concat(disOrderItems5thBeverage.stream(),
            disOrderItemsBev1Snack1.stream()).collect(toList());

    List<MenuItem> menuItems = flattenOrder(disOrderItems);
    int j = flattenOrder(disOrderItems5thBeverage).size();

    for (int i = 0; i < menuItems.size(); i++) {
      MenuItem discountedMenuItem = menuItems.get(i);
      BigDecimal discount = discountedMenuItem.getPrice();
      int discountShift = baseShift - String.format("%.02f", discount).length();

      String discountType = i < j ? "beverage5th" : "beverage1snack1";

      System.out.printf("%-" + discountShift + "s%s%.02f %s", discountType, "-", discount, "CHF");

      if (i < menuItems.size() - 1) {
        System.out.println();
      }
    }

    if (!menuItems.isEmpty()) {
      System.out.println();
    }
    System.out.println("-------------------------------------------");

    BigDecimal billForOrderDisc = billingService.calcSumWithDisc(order, disOrderItems);

    int discountSumShift = baseShift - String.format("%.02f", billForOrderDisc).length();

    System.out.printf("%-" + discountSumShift + "s %.02f %s %n", "Total with discount:",
        billForOrderDisc, "CHF");
  }

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

  private void prettyPrintOrder(List<OrderItem> orderItems) {
    int maxQuantityStrLength = maxQuantityStrLength(orderItems);
    int maxSumPriceStrLength = maxSumPriceStrLength(orderItems);

    orderItems.stream().map(orderItem ->
            printOrder(orderItem, maxQuantityStrLength, maxSumPriceStrLength))
        .forEach(System.out::println);
  }

  private String printOrder(OrderItem orderItem, int maxQuantityLength, int maxSumPriceStrLength) {
    MenuItem menuItem = orderItem.getMenuItem();
    BigDecimal sumPrice = menuItem.getPrice().multiply(new BigDecimal(String.valueOf(orderItem.getQuantity())));

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
        .map(orderItem -> orderItem.getMenuItem().getPrice().multiply(new BigDecimal(String.valueOf(orderItem.getQuantity()))))
        .map(sumPrice -> String.format("%.02f", sumPrice)).map(String::length)
        .max(Integer::compareTo).orElse(0);
  }

  private int maxQuantityAndSumPriceStrLength(List<OrderItem> orders) {
    return orders.stream().map(orderItem -> {
          BigDecimal sumPrice = orderItem.getMenuItem().getPrice().multiply(new BigDecimal(String.valueOf(orderItem.getQuantity())));
          return String.format("%.02f", sumPrice).length() + String.valueOf(orderItem.getQuantity())
              .length();
        })
        .max(Integer::compareTo).orElse(0);
  }
}
