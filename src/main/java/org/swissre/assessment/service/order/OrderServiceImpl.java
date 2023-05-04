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

    String bev5thTitle = "beverage5th";
    String beverage1Snack1Title = "beverage1snack1";

    for (int i = 0; i < disOrderItems.size(); i++) {
      MenuItem discountedMenuItem = disOrderItems.get(i).getMenuItem();
      int quantity = disOrderItems.get(i).getQuantity();
      BigDecimal disc = discountedMenuItem.getPrice();

      int qStrLen = String.valueOf(quantity).length();
      int discShift = baseShift - String.format("%.02f", disc).length() - qStrLen - 3;

      String discType = i < j ? bev5thTitle : beverage1Snack1Title;

      String format = "%-" + discShift + "s%s%.02f %s";
      System.out.printf(format, discType, quantity + " X ", disc.negate(), "CHF");

      if (i < disOrderItems.size() - 1) {
        System.out.println();
      }
    }

    if (!disOrderItems.isEmpty()) {
      System.out.println("\n===========================================");
    }

    printDistSum(baseShift, disOrderItems5thBev, bev5thTitle);

    if (!disOrderItems5thBev.isEmpty() && !disOrderItemsBev1Snack1.isEmpty()) {
      System.out.println("-------------------------------------------");
    }

    printDistSum(baseShift, disOrderItemsBev1Snack1, beverage1Snack1Title);

    System.out.println("-------------------------------------------");

    BigDecimal billForOrderDisc = billingService.calcSumWithDisc(order, disOrderItems);
    int discountSumShift = baseShift - String.format("%.02f", billForOrderDisc).length();

    String format = "%-" + discountSumShift + "s %.02f %s %n";
    System.out.printf(format, "Total with discounts:", billForOrderDisc, "CHF");
  }

  private void printDistSum(int baseShift, List<OrderItem> disOrderItems, String title) {
    if (disOrderItems.isEmpty()) {
      return;
    }
    BigDecimal distSum = billingService.calcSum(disOrderItems);
    int distShift = baseShift - String.format("%.02f", distSum).length();

    String format = "%-" + distShift + "s %.02f %s %n";
    System.out.printf(format, title + " sum:", distSum, "CHF");
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
    String quantityStr = String.valueOf(orderItem.getQuantity());
    BigDecimal sumPrice = menuItem.getPrice().multiply(new BigDecimal(quantityStr));

    int shiftQuantity = maxQuantityLen - quantityStr.length();
    int shiftSumPrice = maxSumPriceStrLen - String.format("%.02f", sumPrice).length();
    int shift = shiftQuantity + shiftSumPrice;

    String spaceBeforePrice = shift > 0 ? "%-" + shift + "s" : "%s";
    String format = "%-14s %-8s %.02f %s %s " + spaceBeforePrice + " %.02f %s";

    return String.format(format, menuItem.getName(), " (" + menuItem.getCode() + ")",
        menuItem.getPrice(), "X", quantityStr, "", sumPrice, "CHF");
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
          BigDecimal price = orderItem.getMenuItem().getPrice();
          BigDecimal sumPrice = price.multiply(new BigDecimal(quantityStr));

          return String.format("%.02f", sumPrice).length() + quantityStr.length();
        })
        .max(Integer::compareTo).orElse(0);
  }
}
