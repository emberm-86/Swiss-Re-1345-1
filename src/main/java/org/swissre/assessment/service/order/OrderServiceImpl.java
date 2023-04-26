package org.swissre.assessment.service.order;

import static org.swissre.assessment.service.menu.MenuUtil.flattenOrder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.swissre.assessment.domain.MenuItem;
import org.swissre.assessment.domain.OrderItem;
import org.swissre.assessment.service.billing.BillingService;
import org.swissre.assessment.service.billing.BillingServiceImpl;
import org.swissre.assessment.service.discount.DiscountService;
import org.swissre.assessment.service.discount.DiscountServiceImpl;

public class OrderServiceImpl implements OrderService {

  List<OrderItem> orderSelectionCurrent = new ArrayList<>();
  OrderStorageProvider orderStorageProvider = new OrderStorageProviderImpl();
  DiscountService discountService = new DiscountServiceImpl();
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

  private void printSingleOrder(Integer orderId, List<OrderItem> order,
      Map<Integer, List<OrderItem>> allOrders) {
    System.out.println("-------------------------------------------");
    prettyPrintOrder(order);
    System.out.println("-------------------------------------------");

    BigDecimal billForOrder = billingService.calcSum(order);
    int baseShift = 32 + maxQuantityAndSumPriceStrLength(order);
    int shift = baseShift - String.format("%.02f", billForOrder).length();

    System.out.printf("%-" + shift + "s %.02f %s %n", "Total:", billForOrder, "CHF");

    List<OrderItem> disOrderItems5thBev = discountService.getDisOrdItems5thBev(orderId, allOrders);
    List<OrderItem> disOrderItemsBev1Snack1 = discountService.getDiscBevSnack1(orderId, allOrders);

    List<OrderItem> disOrderItems = Stream.concat(disOrderItems5thBev.stream(),
        disOrderItemsBev1Snack1.stream()).collect(Collectors.toList());

    List<MenuItem> menuItems = flattenOrder(disOrderItems);
    int j = flattenOrder(disOrderItems5thBev).size();

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

  private void prettyPrintOrder(List<OrderItem> orderItems) {
    int maxQuantityStrLength = maxQuantityStrLength(orderItems);
    int maxSumPriceStrLength = maxSumPriceStrLength(orderItems);

    orderItems.stream().map(orderItem ->
            printOrder(orderItem, maxQuantityStrLength, maxSumPriceStrLength))
        .forEach(System.out::println);
  }

  private String printOrder(OrderItem orderItem, int maxQuantityLength, int maxSumPriceStrLength) {
    MenuItem menuItem = orderItem.getMenuItem();
    BigDecimal sumPrice = menuItem.getPrice()
        .multiply(new BigDecimal(String.valueOf(orderItem.getQuantity())));

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
        .map(orderItem -> orderItem.getMenuItem().getPrice()
            .multiply(new BigDecimal(String.valueOf(orderItem.getQuantity()))))
        .map(sumPrice -> String.format("%.02f", sumPrice)).map(String::length)
        .max(Integer::compareTo).orElse(0);
  }

  private int maxQuantityAndSumPriceStrLength(List<OrderItem> orders) {
    return orders.stream().map(orderItem -> {
          BigDecimal sumPrice = orderItem.getMenuItem().getPrice()
              .multiply(new BigDecimal(String.valueOf(orderItem.getQuantity())));
          return String.format("%.02f", sumPrice).length() + String.valueOf(orderItem.getQuantity())
              .length();
        })
        .max(Integer::compareTo).orElse(0);
  }
}
