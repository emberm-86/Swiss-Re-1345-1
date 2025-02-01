package org.swissre.assessment.service.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
  public void addNewOrderItem(MenuItem menuItemSelected, String menuCode) {
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

    // Do all the calculations here.
    BigDecimal billForOrder = billingService.calcSum(order);

    List<OrderItem> disOrderItems5thBev = discountService.getDisOrdItems5thBev(orderId, allOrders);
    List<OrderItem> disOrderItemsBev1Snack1 = discountService.getDiscBev1Snack1(orderId, allOrders);

    List<OrderItem> disOrderItems = Stream.concat(disOrderItems5thBev.stream(),
            disOrderItemsBev1Snack1.stream()).collect(Collectors.toList());

    BigDecimal billForOrderDisc = billingService.calcSumWithDisc(order, disOrderItems);

    // Provide formatted output.
    int maxQuantityStrLen = maxQuantityStrLen(order);
    int maxSumPriceStrLen = String.format("%.02f", billForOrder).length();

    int baseShift = 34;
    int priceShift = baseShift - 32;
    int rightMargin = baseShift + maxQuantityStrLen + maxSumPriceStrLen;
    int separatorLength = rightMargin + 5;

    printSeparator(separatorLength, '=');
    printHeader(maxQuantityStrLen, maxSumPriceStrLen, priceShift);

    printSeparator(separatorLength, '-');
    prettyPrintOrder(order, maxQuantityStrLen, maxSumPriceStrLen, priceShift);

    int j = disOrderItems5thBev.size();
    int totalShift = rightMargin - String.format("%.02f", billForOrder).length();

    if (!disOrderItems.isEmpty()) {
      printSeparator(separatorLength, '-');
      System.out.printf("%-" + totalShift + "s %.02f %s %n", "Total:", billForOrder, "CHF");

      printSeparator(separatorLength, '=');
      System.out.println("Discounts:");
      printSeparator(separatorLength, '-');
    }

    String bev5thTitle = "beverage5th";
    String beverage1Snack1Title = "beverage1snack1";

    for (int i = 0; i < disOrderItems.size(); i++) {
      MenuItem discountedMenuItem = disOrderItems.get(i).getMenuItem();
      int quantity = disOrderItems.get(i).getQuantity();
      BigDecimal disc = discountedMenuItem.getPrice();

      int qStrLen = String.valueOf(quantity).length();
      int discShift = rightMargin - String.format("%.02f", disc).length() - qStrLen - 3;

      String discType = i < j ? bev5thTitle : beverage1Snack1Title;

      String format = "%-" + discShift + "s%s%.02f %s";
      System.out.printf(format, discType, quantity + " X ", disc.negate(), "CHF");

      if (i < disOrderItems.size() - 1) {
        System.out.println();
      }
    }

    if (!disOrderItems.isEmpty()) {
      System.out.println();
      printSeparator(separatorLength, '-');
    }

    printDistSum(rightMargin, disOrderItems5thBev, bev5thTitle);
    printDistSum(rightMargin, disOrderItemsBev1Snack1, beverage1Snack1Title);
    printSeparator(separatorLength, '=');

    System.out.printf("%-" + totalShift + "s %.02f %s %n", "Total:", billForOrder, "CHF");
    printDistSum(rightMargin, disOrderItems, "All discounts", true);

    int discountSumShift = rightMargin - String.format("%.02f", billForOrderDisc).length();

    String format = "%-" + discountSumShift + "s %.02f %s %n";
    System.out.printf(format, "Total with discounts:", billForOrderDisc, "CHF");
  }

  private static void printHeader(int maxQuantityStrLen, int maxSumPriceStrLen,
                                  int priceShift) {

    String sumQuantityWithSpace = "%" + (10 + maxQuantityStrLen) + "s";
    String sumPriceWithSpace = "%" + (priceShift + 7 + maxSumPriceStrLen) + "s";

    String headerFormat = "%-16s" + "%s" + sumQuantityWithSpace + sumPriceWithSpace;
    System.out.printf((headerFormat) + "%n", "Product", "Code", "Qty X UP", "Sum price");
  }

  private void printSeparator(int shift, char sep) {
    IntStream.range(0, shift).forEach(i -> System.out.print(sep));
    System.out.println();
  }

  private void printDistSum(int baseShift, List<OrderItem> disOrderItems, String title) {
    printDistSum(baseShift, disOrderItems, title, false);
  }

  private void printDistSum(int baseShift, List<OrderItem> disOrderItems, String title,
                            boolean all) {
    if (disOrderItems.isEmpty() && !all) {
      return;
    }

    BigDecimal distSum = billingService.calcSum(disOrderItems);
    int negSignLen = distSum.compareTo(BigDecimal.ZERO) > 0 ? 1 : 0;
    int distShift = baseShift - String.format("%.02f", distSum).length() - negSignLen;

    String format = "%-" + distShift + "s %.02f %s %n";
    System.out.printf(format, title + " sum:", distSum.negate(), "CHF");
  }

  private void prettyPrintOrder(List<OrderItem> orderItems, int maxQuantityStrLen,
                                int maxSumPriceStrLen, int priceShift) {
    orderItems.stream().map(orderItem ->
            createPrintableOrder(orderItem, maxQuantityStrLen, maxSumPriceStrLen, priceShift))
        .forEach(System.out::println);
  }

  private String createPrintableOrder(OrderItem orderItem, int maxQuantityStrLen,
                                      int maxSumPriceStrLen, int priceShift) {
    MenuItem menuItem = orderItem.getMenuItem();
    String quantityStr = String.valueOf(orderItem.getQuantity());
    BigDecimal sumPrice = menuItem.getPrice().multiply(new BigDecimal(quantityStr));

    int shiftQuantity = maxQuantityStrLen - quantityStr.length() + 7;
    int shiftSumPrice = maxSumPriceStrLen - String.format("%.02f", sumPrice).length() + 3
            + priceShift;

    String quantityWithSpace = "%-" + shiftQuantity + "s";
    String sumPriceWithSpace = "%-" + shiftSumPrice + "s";

    String format = "%-16s" + quantityWithSpace + "%s %s %.02f" + sumPriceWithSpace + "%.02f %s";

    return String.format(format, menuItem.getName(), menuItem.getCode(),
        quantityStr, "X", menuItem.getPrice(), "", sumPrice, "CHF");
  }

  private int maxQuantityStrLen(List<OrderItem> orders) {
    return orders.stream()
        .map(OrderItem::getQuantity)
        .map(String::valueOf)
        .map(String::length)
        .max(Integer::compareTo).orElse(0);
  }
}
