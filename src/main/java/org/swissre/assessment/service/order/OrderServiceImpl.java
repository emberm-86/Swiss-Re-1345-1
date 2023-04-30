package org.swissre.assessment.service.order;

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
    int baseShift = 32 + maxQuantityAndSumPriceStrLen(order);
    int shift = baseShift - String.format("%.02f", billForOrder).length();

    System.out.printf("%-" + shift + "s %.02f %s %n", "Total:", billForOrder, "CHF");

    List<OrderItem> disOrderItems5thBev = discountService.getDisOrdItems5thBev(orderId, allOrders);
    List<OrderItem> disOrderItemsBev1Snack1 = discountService.getDiscBev1Snack1(orderId, allOrders);

    List<OrderItem> disOrderItems = Stream.concat(disOrderItems5thBev.stream(),
        disOrderItemsBev1Snack1.stream()).collect(Collectors.toList());

    int j = disOrderItems5thBev.size();

    if (!disOrderItems.isEmpty()) {
      System.out.println("-------------------------------------------");
      System.out.println("Discounts:\n-------------------------------------------");
    }

    for (int i = 0; i < disOrderItems.size(); i++) {
      MenuItem discountedMenuItem = disOrderItems.get(i).getMenuItem();
      int quantity = disOrderItems.get(i).getQuantity();
      BigDecimal disc = discountedMenuItem.getPrice();

      int qStrLen = String.valueOf(quantity).length();
      int discShift = baseShift - String.format("%.02f", disc).length() - qStrLen - 3;

      String discType = i < j ? "beverage5th" : "beverage1snack1";

      String format = "%-" + discShift + "s%s%s%.02f %s";
      System.out.printf(format, discType, quantity + " X ", "-", disc, "CHF");

      if (i < disOrderItems.size() - 1) {
        System.out.println();
      }
    }

    if (!disOrderItems.isEmpty()) {
      System.out.println();
    }
    System.out.println("-------------------------------------------");

    BigDecimal billForOrderDisc = billingService.calcSumWithDisc(order, disOrderItems);

    int discountSumShift = baseShift - String.format("%.02f", billForOrderDisc).length();

    String format = "%-" + discountSumShift + "s %.02f %s %n";
    System.out.printf(format, "Total with discounts:", billForOrderDisc, "CHF");
  }

  private void prettyPrintOrder(List<OrderItem> orderItems) {
    int maxQuantityStrLen = maxQuantityStrLen(orderItems);
    int maxSumPriceStrLen = maxSumPriceStrLen(orderItems);

    orderItems.stream().map(orderItem ->
            printOrder(orderItem, maxQuantityStrLen, maxSumPriceStrLen))
        .forEach(System.out::println);
  }

  private String printOrder(OrderItem orderItem, int maxQuantityLen, int maxSumPriceStrLen) {
    MenuItem menuItem = orderItem.getMenuItem();
    BigDecimal sumPrice = menuItem.getPrice()
        .multiply(new BigDecimal(String.valueOf(orderItem.getQuantity())));

    int shiftQuantity = maxQuantityLen - String.valueOf(orderItem.getQuantity()).length();
    int shiftSumPrice = maxSumPriceStrLen - String.format("%.02f", sumPrice).length();
    int shift = shiftQuantity + shiftSumPrice;

    String spaceBeforePrice = shift > 0 ? "%-" + shift + "s" : "%s";

    return String.format("%-14s %-8s %.02f %s %d " + spaceBeforePrice + " %.02f %s",
        menuItem.getName(), " (" + menuItem.getCode() + ")",
        menuItem.getPrice(), "X", orderItem.getQuantity(), "", sumPrice, "CHF");
  }

  private int maxQuantityStrLen(List<OrderItem> orders) {
    return orders.stream().map(OrderItem::getQuantity).map(String::valueOf).map(String::length)
        .max(Integer::compareTo).orElse(0);
  }

  private int maxSumPriceStrLen(List<OrderItem> orders) {
    return orders.stream()
        .map(orderItem -> orderItem.getMenuItem().getPrice()
            .multiply(new BigDecimal(String.valueOf(orderItem.getQuantity()))))
        .map(sumPrice -> String.format("%.02f", sumPrice)).map(String::length)
        .max(Integer::compareTo).orElse(0);
  }

  private int maxQuantityAndSumPriceStrLen(List<OrderItem> orders) {
    return orders.stream().map(orderItem -> {
          String quantityStr = String.valueOf(orderItem.getQuantity());
          BigDecimal sumPrice = orderItem.getMenuItem().getPrice()
              .multiply(new BigDecimal(quantityStr));
          return String.format("%.02f", sumPrice).length() + quantityStr.length();
        })
        .max(Integer::compareTo).orElse(0);
  }
}
