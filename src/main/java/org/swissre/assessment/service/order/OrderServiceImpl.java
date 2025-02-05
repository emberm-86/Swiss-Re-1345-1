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

import static org.swissre.assessment.domain.Constants.CURRENCY;
import static org.swissre.assessment.domain.Constants.FLT_FMT;

public class OrderServiceImpl implements OrderService {

  private static final int BASE_SHIFT = 35;
  private static final int PRC_SHIFT = BASE_SHIFT - 33;
  private static final int QTY_SHIFT = 8;

  List<OrderItem> ordSelectionCurrent = new ArrayList<>();
  OrderStorageProvider ordStgProvider = new OrderStorageProviderImpl();
  DiscountService discountService = new DiscountServiceImpl();
  BillingService billingService = new BillingServiceImpl();

  @Override
  public void addNewOrderItem(MenuItem menuItemSelected, String menuCode) {
    ordSelectionCurrent.add(new OrderItem(menuItemSelected, Integer.parseInt(menuCode)));
  }

  @Override
  public void closeOrder() {
    if (ordSelectionCurrent.isEmpty()) {
      System.out.println("You have ordered nothing.");
      return;
    }

    ordStgProvider.addNewOrder(new ArrayList<>(ordSelectionCurrent));

    System.out.println("You can check your bill here.");

    printSingleOrder(
        ordStgProvider.getLastOrderIndex(), ordSelectionCurrent, ordStgProvider.getAllOrders());

    ordSelectionCurrent.clear();
  }

  @Override
  public void printAllOrders() {
    System.out.println("========================");

    Map<Integer, List<OrderItem>> allOrders = ordStgProvider.getAllOrders();

    if (allOrders.isEmpty()) {
      System.out.println("There is no order in the system.");
    }

    allOrders.forEach(
        (orderId, order) -> {
          if (orderId > 0) {
            System.out.println();
          }

          System.out.println("Order: " + orderId);
          printSingleOrder(orderId, order, allOrders);
        });
    System.out.println("========================\n");
  }

  private void printSingleOrder(
      Integer orderId, List<OrderItem> order, Map<Integer, List<OrderItem>> allOrders) {

    // Do all the calculations here.
    BigDecimal billForOrder = billingService.calcSum(order);

    List<OrderItem> disOrderItems5thBev = discountService.getDisOrdItems5thBev(orderId, allOrders);
    List<OrderItem> disOrderItemsBev1Snack1 = discountService.getDiscBev1Snack1(orderId, allOrders);

    List<OrderItem> disOrderItems =
        Stream.concat(disOrderItems5thBev.stream(), disOrderItemsBev1Snack1.stream())
            .collect(Collectors.toList());

    BigDecimal billForOrderDisc = billingService.calcSumWithDisc(order, disOrderItems);

    // Provide formatted output.
    int maxQtyStrLen = maxQtyStrLen(order);
    int maxSumPrcStrLen = String.format(FLT_FMT, billForOrder).length();
    if (!disOrderItems.isEmpty() && maxSumPrcStrLen < 5) {
      maxSumPrcStrLen = 5;
    }

    int rightMargin = BASE_SHIFT + maxQtyStrLen + maxSumPrcStrLen;
    int separatorLength = rightMargin + 5;

    printSeparator(separatorLength, '=');
    printHeader(maxQtyStrLen, maxSumPrcStrLen);

    printSeparator(separatorLength, '-');
    prettyPrintOrder(order, maxQtyStrLen, maxSumPrcStrLen);

    int j = disOrderItems5thBev.size();
    int totalShift = rightMargin - String.format(FLT_FMT, billForOrder).length();

    if (!disOrderItems.isEmpty()) {
      printSeparator(separatorLength, '-');
      System.out.printf(
          "%-" + totalShift + "s " + FLT_FMT + " %s %n", "Total:", billForOrder, CURRENCY);

      printSeparator(separatorLength, '=');
      System.out.println("Discounts:");
      printSeparator(separatorLength, '-');
    }

    String bev5thTitle = "beverage5th";
    String beverage1Snack1Title = "beverage1snack1";

    for (int i = 0; i < disOrderItems.size(); i++) {
      MenuItem discountedMenuItem = disOrderItems.get(i).getMenuItem();
      int qty = disOrderItems.get(i).getQuantity();
      BigDecimal disc = discountedMenuItem.getPrice();

      int qStrLen = String.valueOf(qty).length();
      int discShift = rightMargin - String.format(FLT_FMT, disc).length() - qStrLen - 3;

      String discType = i < j ? bev5thTitle : beverage1Snack1Title;

      String format = "%-" + discShift + "s%s" + FLT_FMT + " %s";
      System.out.printf(format, discType, qty + " X ", disc.negate(), CURRENCY);

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

    System.out.printf(
        "%-" + totalShift + "s " + FLT_FMT + " %s %n", "Total:", billForOrder, CURRENCY);
    printDistSum(rightMargin, disOrderItems, "All discounts", true);

    int discountSumShift = rightMargin - String.format(FLT_FMT, billForOrderDisc).length();

    String format = "%-" + discountSumShift + "s " + FLT_FMT + " %s %n";
    System.out.printf(format, "Total with discounts:", billForOrderDisc, CURRENCY);
  }

  private static void printHeader(int maxQtyStrLen, int maxSumPrcStrLen) {
    String sumQtyWithSpace = "%" + (QTY_SHIFT + 3 + maxQtyStrLen) + "s";
    String sumPrcWithSpace = "%" + (PRC_SHIFT + 7 + maxSumPrcStrLen) + "s";

    String headerFormat = "%-16s" + "%s" + sumQtyWithSpace + sumPrcWithSpace;
    System.out.printf((headerFormat) + "%n", "Product", "Code", "Qty X UP", "Sum price");
  }

  private void printSeparator(int shift, char sep) {
    IntStream.range(0, shift).forEach(i -> System.out.print(sep));
    System.out.println();
  }

  private void printDistSum(int baseShift, List<OrderItem> disOrderItems, String title) {
    printDistSum(baseShift, disOrderItems, title, false);
  }

  private void printDistSum(int baseShift, List<OrderItem> disOrdItems, String title, boolean all) {
    if (disOrdItems.isEmpty() && !all) {
      return;
    }

    BigDecimal distSum = billingService.calcSum(disOrdItems);
    int negSignLen = distSum.compareTo(BigDecimal.ZERO) > 0 ? 1 : 0;
    int distShift = baseShift - String.format(FLT_FMT, distSum).length() - negSignLen;

    String format = "%-" + distShift + "s " + FLT_FMT + " %s %n";
    System.out.printf(format, title + " sum:", distSum.negate(), CURRENCY);
  }

  private void prettyPrintOrder(List<OrderItem> orderItems, int maxQtyStrLen, int maxSumPrcStrLen) {
    orderItems.stream()
        .map(orderItem -> createPrintableOrder(orderItem, maxQtyStrLen, maxSumPrcStrLen))
        .forEach(System.out::println);
  }

  private String createPrintableOrder(OrderItem orderItem, int maxQtyStrLen, int maxSumPrcStrLen) {
    MenuItem menuItem = orderItem.getMenuItem();
    String qtyStr = String.valueOf(orderItem.getQuantity());
    BigDecimal sumPrice = menuItem.getPrice().multiply(new BigDecimal(qtyStr));

    int shiftQty = maxQtyStrLen - qtyStr.length() + QTY_SHIFT;
    int shiftSumPrc = maxSumPrcStrLen - String.format(FLT_FMT, sumPrice).length() + 3 + PRC_SHIFT;

    String qtyWithSpace = "%-" + shiftQty + "s";
    String sumPrcWithSpace = "%-" + shiftSumPrc + "s";

    String format = "%-16s" + qtyWithSpace + "%s %s " + FLT_FMT + sumPrcWithSpace + FLT_FMT + " %s";

    return String.format(
        format,
        menuItem.getName(),
        menuItem.getCode(),
        qtyStr,
        "X",
        menuItem.getPrice(),
        "",
        sumPrice,
        CURRENCY);
  }

  private int maxQtyStrLen(List<OrderItem> orders) {
    return orders.stream()
        .map(OrderItem::getQuantity)
        .map(String::valueOf)
        .map(String::length)
        .max(Integer::compareTo)
        .orElse(0);
  }
}
