package org.swissre.assessment.service.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swissre.assessment.domain.MenuItem;
import org.swissre.assessment.domain.OrderItem;
import org.swissre.assessment.domain.datastructure.OrderMap;
import org.swissre.assessment.service.billing.BillingService;
import org.swissre.assessment.service.billing.BillingServiceImpl;
import org.swissre.assessment.service.discount.Discount5thBevServiceImpl;
import org.swissre.assessment.service.discount.DiscountBev1Snack1ServiceImpl;
import org.swissre.assessment.service.discount.DiscountService;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.swissre.assessment.domain.Constants.*;

public class SingleOrderPrinterServiceImpl implements SingleOrderPrinterService {

  private static final Logger LOGGER = LoggerFactory.getLogger(SingleOrderPrinterServiceImpl.class);

  private static final int BASE_SHIFT = 35;
  private static final int PRC_SHIFT = BASE_SHIFT - 33;
  private static final int QTY_SHIFT = 8;

  DiscountService discount5thBevService = new Discount5thBevServiceImpl();
  DiscountService discountBev1Snack1Service = new DiscountBev1Snack1ServiceImpl();
  BillingService billingService = new BillingServiceImpl();

  @Override
  public void print(Integer orderId, List<OrderItem> order, OrderMap allOrders) {
    // Done all the calculations here.
    BigDecimal billForOrder = billingService.calcSum(order);

    List<OrderItem> disOrderItems5thBev =
        discount5thBevService.getDiscountedOrderItems(orderId, allOrders);

    List<OrderItem> disOrderItemsBev1Snack1 =
        discountBev1Snack1Service.getDiscountedOrderItems(orderId, allOrders);

    List<OrderItem> disOrderItems =
        Stream.concat(disOrderItems5thBev.stream(), disOrderItemsBev1Snack1.stream()).toList();

    BigDecimal billForOrderDisc = billingService.calcSumWithDisc(order, disOrderItems);

    // Output formatting.
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

      String formatTotal = "%-" + totalShift + "s " + FLT_FMT + " %s";
      String totalRow = String.format(formatTotal, "Total:", billForOrder, CURRENCY);
      LOGGER.info(totalRow);

      printSeparator(separatorLength, '=');

      String discountsLabelStr = "Discounts:";
      String discountFormat = "%-" + (rightMargin + 4) + "s ";
      String discountLabel = String.format(discountFormat, discountsLabelStr);
      LOGGER.info(discountLabel);

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

      String formatDiscount = "%-" + discShift + "s%s" + FLT_FMT + " %s";
      String discountRow =
          String.format(formatDiscount, discType, qty + " X ", disc.negate(), CURRENCY);
      LOGGER.info(discountRow);
    }

    if (!disOrderItems.isEmpty()) {
      printSeparator(separatorLength, '-');
    }

    printDistSum(rightMargin, disOrderItems5thBev, bev5thTitle);
    printDistSum(rightMargin, disOrderItemsBev1Snack1, beverage1Snack1Title);
    printSeparator(separatorLength, '=');

    String formatTotal = "%-" + totalShift + "s " + FLT_FMT + " %s";
    String totalRow = String.format(formatTotal, "Total:", billForOrder, CURRENCY);
    LOGGER.info(totalRow);

    printDistSum(rightMargin, disOrderItems, "All discounts", true);

    int discountSumShift = rightMargin - String.format(FLT_FMT, billForOrderDisc).length();
    String formatTotalDisc = "%-" + discountSumShift + "s " + FLT_FMT + " %s";
    String totalWithDiscountsRow =
        String.format(formatTotalDisc, "Total with discounts:", billForOrderDisc, CURRENCY);
    LOGGER.info(totalWithDiscountsRow);
  }

  private static void printHeader(int maxQtyStrLen, int maxSumPrcStrLen) {
    String sumQtyWithSpace = "%" + (QTY_SHIFT + 3 + maxQtyStrLen) + "s";
    String sumPrcWithSpace = "%" + (PRC_SHIFT + 7 + maxSumPrcStrLen) + "s";

    String headerFormat = "%-16s" + "%s" + sumQtyWithSpace + sumPrcWithSpace;
    String headerRow = String.format((headerFormat), "Product", "Code", "Qty X UP", "Sum price");
    LOGGER.info(headerRow);
  }

  private void printSeparator(int shift, char sep) {
    StringBuilder sb = new StringBuilder();
    IntStream.range(0, shift).forEach(i -> sb.append(sep));
    String separatorString = sb.toString();
    LOGGER.info(separatorString);
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

    String format = "%-" + distShift + "s " + FLT_FMT + " %s";
    String distSumRow = String.format(format, title + " sum:", distSum.negate(), CURRENCY);
    LOGGER.info(distSumRow);
  }

  private void prettyPrintOrder(List<OrderItem> orderItems, int maxQtyStrLen, int maxSumPrcStrLen) {
    orderItems.stream()
        .map(orderItem -> createPrintableOrder(orderItem, maxQtyStrLen, maxSumPrcStrLen))
        .forEach(LOGGER::info);
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
