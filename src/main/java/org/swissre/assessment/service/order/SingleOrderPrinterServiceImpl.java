package org.swissre.assessment.service.order;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.swissre.assessment.domain.MenuItem;
import org.swissre.assessment.domain.OrderItem;
import org.swissre.assessment.domain.datastructure.OrderMap;
import org.swissre.assessment.service.billing.BillingService;
import org.swissre.assessment.service.billing.BillingServiceImpl;
import org.swissre.assessment.service.discount.Discount5thBevServiceImpl;
import org.swissre.assessment.service.discount.DiscountBev1Snack1ServiceImpl;
import org.swissre.assessment.service.discount.DiscountService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.swissre.assessment.domain.Constants.*;
import static org.swissre.assessment.service.util.MenuPrinter.printLoggingInfoDisabled;

public class SingleOrderPrinterServiceImpl implements SingleOrderPrinterService {

  private static final Logger LOGGER = LogManager.getLogger(SingleOrderPrinterServiceImpl.class);

  private static final int BASE_SHIFT = 35;
  private static final int PRC_SHIFT = BASE_SHIFT - 33;
  private static final int QTY_SHIFT = 8;

  private static final String ORDER_DATE =
      LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
  private static final String ORDER_TIME =
      LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

  private static final List<String> RECEIPT_TITLE =
      Arrays.asList("Charlene's Coffee Corner", "Swiss Re Office", "Soodring 6, 8134 Adliswil, ZH");

  DiscountService discount5thBevService = new Discount5thBevServiceImpl();
  DiscountService discountBev1Snack1Service = new DiscountBev1Snack1ServiceImpl();
  BillingService billingService = new BillingServiceImpl();

  @Override
  public void print(Integer orderId, List<OrderItem> order, OrderMap allOrders, boolean receipt) {
    if (!LOGGER.isInfoEnabled()) {
      printLoggingInfoDisabled(LOGGER);
      return;
    }

    // Do all the calculations here.
    BigDecimal billForOrder = billingService.calcSum(order);

    List<OrderItem> disOrderItems5thBev =
        discount5thBevService.getDiscountedOrderItems(orderId, allOrders);

    List<OrderItem> disOrderItemsBev1Snack1 =
        discountBev1Snack1Service.getDiscountedOrderItems(orderId, allOrders);

    List<OrderItem> disOrderItems =
        Stream.concat(disOrderItems5thBev.stream(), disOrderItemsBev1Snack1.stream()).toList();

    BigDecimal billForOrderDisc = billingService.calcSumWithDisc(order, disOrderItems);

    // Provide formatted output.
    int maxQtyStrLen = maxQtyStrLen(order);
    int maxSumPrcStrLen = String.format(FLT_FMT, billForOrder).length();
    if (!disOrderItems.isEmpty() && maxSumPrcStrLen < 5) {
      maxSumPrcStrLen = 5;
    }

    int rightMargin = BASE_SHIFT + maxQtyStrLen + maxSumPrcStrLen;
    int separatorLength = rightMargin + (receipt ? 9 : 5);

    if (receipt) {
      printReceiptHeader(separatorLength);
    }

    printSeparator(separatorLength, '=', receipt);
    printHeader(maxQtyStrLen, maxSumPrcStrLen, receipt);

    printSeparator(separatorLength, '-', receipt);
    prettyPrintOrder(order, maxQtyStrLen, maxSumPrcStrLen, receipt);

    int j = disOrderItems5thBev.size();
    int totalShift = rightMargin - String.format(FLT_FMT, billForOrder).length();

    if (!disOrderItems.isEmpty()) {
      printSeparator(separatorLength, '-', receipt);
      String formatTotal = "%-" + totalShift + "s " + FLT_FMT + " %s";
      String totalRow = String.format(formatTotal, "Total:", billForOrder, CURRENCY);
      LOGGER.info(createRow(receipt, totalRow));

      printSeparator(separatorLength, '=', receipt);
      String discountsLabelStr = "Discounts:";
      String discountFormat = "%-" + (rightMargin + 4) + "s ";
      String discountLabel = String.format(discountFormat, discountsLabelStr);
      LOGGER.info(createRow(receipt, discountLabel));
      printSeparator(separatorLength, '-', receipt);
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
      LOGGER.info(createRow(receipt, discountRow));
    }

    if (!disOrderItems.isEmpty()) {
      printSeparator(separatorLength, '-', receipt);
    }

    printDistSum(rightMargin, disOrderItems5thBev, bev5thTitle, receipt);
    printDistSum(rightMargin, disOrderItemsBev1Snack1, beverage1Snack1Title, receipt);
    printSeparator(separatorLength, '=', receipt);

    String formatTotal = "%-" + totalShift + "s " + FLT_FMT + " %s";
    String totalRow = String.format(formatTotal, "Total:", billForOrder, CURRENCY);
    LOGGER.info(createRow(receipt, totalRow));

    printDistSum(rightMargin, disOrderItems, "All discounts", true, receipt);

    int discountSumShift = rightMargin - String.format(FLT_FMT, billForOrderDisc).length();
    String formatTotalDisc = "%-" + discountSumShift + "s " + FLT_FMT + " %s";
    String totalWithDiscountsRow =
        String.format(formatTotalDisc, "Total with discounts:", billForOrderDisc, CURRENCY);
    LOGGER.info(createRow(receipt, totalWithDiscountsRow));

    if (receipt) {
      printReceiptFooter(separatorLength);
    }
  }

  private void printReceiptHeader(int separatorLength) {
    printSeparator(separatorLength, '=', true);
    IntStream.range(0, RECEIPT_TITLE.size()).forEach(i -> printReceiptTitle(separatorLength, i));
  }

  private void printReceiptTitle(int separatorLength, int i) {
    if (!LOGGER.isInfoEnabled()) {
      printLoggingInfoDisabled(LOGGER);
      return;
    }

    String receiptTitlePart = RECEIPT_TITLE.get(i);
    int minorShift = separatorLength % 2 == 0 ? 1 : 0;
    String format =
        "%s%"
            + (separatorLength / 2 + (receiptTitlePart.length() / 2) - minorShift)
            + "s"
            + "%"
            + (separatorLength / 2 - (receiptTitlePart.length() / 2))
            + "s";
    String receiptTitleRow = String.format(format, "|", receiptTitlePart, "|");
    LOGGER.info(createRow(false, receiptTitleRow));
  }

  private void printReceiptFooter(int separatorLength) {
    printSeparator(separatorLength, '=', true);
    printReceiptFooterRow(separatorLength, "Time:", ORDER_TIME);
    printReceiptFooterRow(separatorLength, "Date:", ORDER_DATE);
    printReceiptFooterRow(separatorLength, "Receipt #:", createUniqueReceiptNumber());
    printSeparator(separatorLength, '=', true);
  }

  void printReceiptFooterRow(int separatorLength, String fieldName, String fieldValue) {
    if (!LOGGER.isInfoEnabled()) {
      printLoggingInfoDisabled(LOGGER);
      return;
    }

    String footerFormat = "%-" + (separatorLength - fieldValue.length() - 5) + "s %s";
    String footerRow = String.format(footerFormat, fieldName, fieldValue);
    LOGGER.info(createRow(true, footerRow));
  }

  private String createUniqueReceiptNumber() {
    return Integer.toHexString((int) System.currentTimeMillis()).toUpperCase();
  }

  private static void printHeader(int maxQtyStrLen, int maxSumPrcStrLen, boolean receipt) {
    if (!LOGGER.isInfoEnabled()) {
      printLoggingInfoDisabled(LOGGER);
      return;
    }

    String sumQtyWithSpace = "%" + (QTY_SHIFT + 3 + maxQtyStrLen) + "s";
    String sumPrcWithSpace = "%" + (PRC_SHIFT + 7 + maxSumPrcStrLen) + "s";

    String headerFormat = "%-16s" + "%s" + sumQtyWithSpace + sumPrcWithSpace;
    String headerRow = String.format((headerFormat), "Product", "Code", "Qty X UP", "Sum price");
    LOGGER.info(createRow(receipt, headerRow));
  }

  private void printSeparator(int shift, char sep, boolean receipt) {
    if (!LOGGER.isInfoEnabled()) {
      printLoggingInfoDisabled(LOGGER);
      return;
    }

    StringBuilder sb = new StringBuilder();
    if (receipt) {
      sb.append("|");
      IntStream.range(0, shift - 2).forEach(i -> sb.append(sep));
      sb.append("|");
    } else {
      IntStream.range(0, shift).forEach(i -> sb.append(sep));
    }
    LOGGER.info(sb.toString());
  }

  private void printDistSum(
      int baseShift, List<OrderItem> disOrderItems, String title, boolean receipt) {
    printDistSum(baseShift, disOrderItems, title, false, receipt);
  }

  private void printDistSum(
      int baseShift, List<OrderItem> disOrdItems, String title, boolean all, boolean receipt) {

    if (!LOGGER.isInfoEnabled()) {
      LOGGER.error(LOG_INFO_DISABLED);
      return;
    }

    if (disOrdItems.isEmpty() && !all) {
      return;
    }

    BigDecimal distSum = billingService.calcSum(disOrdItems);
    int negSignLen = distSum.compareTo(BigDecimal.ZERO) > 0 ? 1 : 0;
    int distShift = baseShift - String.format(FLT_FMT, distSum).length() - negSignLen;

    String format = "%-" + distShift + "s " + FLT_FMT + " %s";
    String distSumRow = String.format(format, title + " sum:", distSum.negate(), CURRENCY);
    LOGGER.info(createRow(receipt, distSumRow));
  }

  private void prettyPrintOrder(
      List<OrderItem> orderItems, int maxQtyStrLen, int maxSumPrcStrLen, boolean receipt) {
    orderItems.stream()
        .map(orderItem -> createPrintableOrder(orderItem, maxQtyStrLen, maxSumPrcStrLen, receipt))
        .forEach(LOGGER::info);
  }

  private String createPrintableOrder(
      OrderItem orderItem, int maxQtyStrLen, int maxSumPrcStrLen, boolean receipt) {
    MenuItem menuItem = orderItem.getMenuItem();
    String qtyStr = String.valueOf(orderItem.getQuantity());
    BigDecimal sumPrice = menuItem.getPrice().multiply(new BigDecimal(qtyStr));

    int shiftQty = maxQtyStrLen - qtyStr.length() + QTY_SHIFT;
    int shiftSumPrc = maxSumPrcStrLen - String.format(FLT_FMT, sumPrice).length() + 3 + PRC_SHIFT;

    String qtyWithSpace = "%-" + shiftQty + "s";
    String sumPrcWithSpace = "%-" + shiftSumPrc + "s";

    String format = "%-16s" + qtyWithSpace + "%s %s " + FLT_FMT + sumPrcWithSpace + FLT_FMT + " %s";

    String orderItemRow =
        String.format(
            format,
            menuItem.getName(),
            menuItem.getCode(),
            qtyStr,
            "X",
            menuItem.getPrice(),
            "",
            sumPrice,
            CURRENCY);

    return createRow(receipt, orderItemRow);
  }

  private static String createRow(boolean receipt, String row) {
    return receipt ? "| " + row + " |" : row;
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
