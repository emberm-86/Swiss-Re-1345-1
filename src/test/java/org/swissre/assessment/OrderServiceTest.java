package org.swissre.assessment;

import static java.math.BigDecimal.ZERO;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.swissre.assessment.domain.MenuItem;
import org.swissre.assessment.domain.OrderItem;
import org.swissre.assessment.service.billing.BillingService;
import org.swissre.assessment.service.billing.BillingServiceImpl;
import org.swissre.assessment.service.order.OrderService;
import org.swissre.assessment.service.order.OrderServiceImpl;

public class OrderServiceTest {

  OrderService orderService = new OrderServiceImpl();
  BillingService billingService = new BillingServiceImpl();

  @ParameterizedTest
  @MethodSource("createOneComplexOrder")
  public void test5thBeverageInSameOrder(Map<Integer, List<OrderItem>> orders) {
    List<OrderItem> disOrds5thBev = orderService.getDisOrdItems5thBev(
        orders.size() - 1, orders);

    assertEquals(2, disOrds5thBev.size());
    assertEquals(disOrds5thBev.get(0), new OrderItem(MenuItem.MEDIUM_COFFEE, 1));
    assertEquals(disOrds5thBev.get(1), new OrderItem(MenuItem.ORANGE_JUICE, 1));

    BigDecimal origSumPrice = billingService.calcSum(orders.get(0));
    BigDecimal discSumPrice = billingService.calcSumWithDisc(orders.get(0), disOrds5thBev);
    BigDecimal expectedDiff = disOrds5thBev.stream()
        .map(orderItem -> orderItem.getMenuItem().getPrice()
            .multiply(new BigDecimal(String.valueOf(orderItem.getQuantity()))))
        .reduce(ZERO, BigDecimal::add);

    assertEquals(expectedDiff, origSumPrice.subtract(discSumPrice));
  }

  @ParameterizedTest
  @MethodSource("createMultipleOrders")
  public void test5thBeverageInSameOrderMultipleOrders(Map<Integer, List<OrderItem>> orders) {
    List<OrderItem> discOrders1 = orderService.getDisOrdItems5thBev(0, orders);
    List<OrderItem> discOrders2 = orderService.getDisOrdItems5thBev(1, orders);
    List<OrderItem> discOrders3 = orderService.getDisOrdItems5thBev(2, orders);

    assertEquals(1, discOrders1.size());
    assertEquals(1, discOrders2.size());
    assertEquals(2, discOrders3.size());

    assertEquals(discOrders1.get(0), new OrderItem(MenuItem.MEDIUM_COFFEE, 1));
    assertEquals(discOrders2.get(0), new OrderItem(MenuItem.ORANGE_JUICE, 1));
    assertEquals(discOrders3.get(0), new OrderItem(MenuItem.LARGE_COFFEE, 1));
    assertEquals(discOrders3.get(1), new OrderItem(MenuItem.MEDIUM_COFFEE, 1));

    for (int i = 0; i < orders.size(); i++) {
      List<OrderItem> disOrdItems5thBev = orderService.getDisOrdItems5thBev(i, orders);
      BigDecimal origSumPrice = billingService.calcSum(orders.get(i));
      BigDecimal discSumPrice = billingService.calcSumWithDisc(orders.get(i), disOrdItems5thBev);

      BigDecimal expectedDiff = disOrdItems5thBev.stream()
          .map(orderItem -> orderItem.getMenuItem().getPrice()
              .multiply(new BigDecimal(String.valueOf(orderItem.getQuantity()))))
          .reduce(new BigDecimal("0.00"), BigDecimal::add);

      assertEquals(expectedDiff, origSumPrice.subtract(discSumPrice));
    }
  }

  @ParameterizedTest
  @MethodSource("createOneComplexOrder")
  public void testBeverage1Snack1(Map<Integer, List<OrderItem>> orders) {
    List<OrderItem> discountedBeverage1Snack1 = orderService.getDiscountsBeverage1Snack1(0, orders);

    assertEquals(1, discountedBeverage1Snack1.size());
    assertEquals(discountedBeverage1Snack1.get(0), new OrderItem(MenuItem.EXTRA_MILK, 2));

    List<OrderItem> order1 = orders.get(0);
    BigDecimal origSumPrice = billingService.calcSum(order1);
    BigDecimal discSumPrice = billingService.calcSumWithDisc(order1,
        discountedBeverage1Snack1);

    BigDecimal expectedDiff = discountedBeverage1Snack1.stream()
        .map(orderItem -> orderItem.getMenuItem().getPrice()
            .multiply(new BigDecimal(String.valueOf(orderItem.getQuantity()))))
        .reduce(new BigDecimal("0.00"), BigDecimal::add);

    assertEquals(expectedDiff, origSumPrice.subtract(discSumPrice));
  }

  @ParameterizedTest
  @MethodSource("createOrdersNoDiscount")
  public void test5thBeverageInSameOrderNoDiscount(Map<Integer, List<OrderItem>> orders) {
    List<OrderItem> discOrders5thBeverage = orderService.getDisOrdItems5thBev(0, orders);

    assertTrue(discOrders5thBeverage.isEmpty());

    List<OrderItem> order1 = orders.get(0);
    BigDecimal origSumPrice = billingService.calcSum(order1);
    BigDecimal discSumPrice = billingService.calcSumWithDisc(order1, discOrders5thBeverage);

    assertEquals(origSumPrice, discSumPrice);
  }

  @ParameterizedTest
  @MethodSource("createOrdersNoDiscount")
  public void testBeverage1Snack1NoDiscount(Map<Integer, List<OrderItem>> orders) {
    List<OrderItem> discountedBeverage1Snack1 = orderService.getDiscountsBeverage1Snack1(0, orders);

    assertTrue(discountedBeverage1Snack1.isEmpty());

    List<OrderItem> order1 = orders.get(0);
    BigDecimal origSumPrice = billingService.calcSum(order1);
    BigDecimal discSumPrice = billingService.calcSumWithDisc(order1, discountedBeverage1Snack1);

    assertEquals(origSumPrice, discSumPrice);
  }

  private static Stream<Arguments> createOneComplexOrder() {
    return Stream.of(
        Arguments.of(Stream.of(
                new SimpleEntry<>(0, asList(
                    new OrderItem(MenuItem.BACON_ROLL, 2),
                    new OrderItem(MenuItem.SMALL_COFFEE, 1),
                    new OrderItem(MenuItem.ORANGE_JUICE, 2),
                    new OrderItem(MenuItem.MEDIUM_COFFEE, 3),
                    new OrderItem(MenuItem.ORANGE_JUICE, 4),
                    new OrderItem(MenuItem.EXTRA_MILK, 1),
                    new OrderItem(MenuItem.FOAMED_MILK, 1),
                    new OrderItem(MenuItem.ROASTED_COFFEE, 1),
                    new OrderItem(MenuItem.EXTRA_MILK, 1))))
            .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue))
        ));
  }

  private static Stream<Arguments> createMultipleOrders() {
    return Stream.of(
        Arguments.of(Stream.of(
                new SimpleEntry<>(0, asList(
                    new OrderItem(MenuItem.BACON_ROLL, 2),
                    new OrderItem(MenuItem.SMALL_COFFEE, 1),
                    new OrderItem(MenuItem.ORANGE_JUICE, 2),
                    new OrderItem(MenuItem.MEDIUM_COFFEE, 3),
                    new OrderItem(MenuItem.ORANGE_JUICE, 2),
                    new OrderItem(MenuItem.EXTRA_MILK, 1),
                    new OrderItem(MenuItem.FOAMED_MILK, 1),
                    new OrderItem(MenuItem.ROASTED_COFFEE, 1),
                    new OrderItem(MenuItem.EXTRA_MILK, 1))),

                new SimpleEntry<>(1, Arrays.asList(
                    new OrderItem(MenuItem.ORANGE_JUICE, 3),
                    new OrderItem(MenuItem.BACON_ROLL, 4))),

                new SimpleEntry<>(2, Arrays.asList(
                    new OrderItem(MenuItem.LARGE_COFFEE, 4),
                    new OrderItem(MenuItem.MEDIUM_COFFEE, 5))))
            .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue))
        ));
  }

  private static Stream<Arguments> createOrdersNoDiscount() {
    return Stream.of(
        Arguments.of(Stream.of(
                new SimpleEntry<>(0, Collections.singletonList(
                    new OrderItem(MenuItem.ORANGE_JUICE, 4))))
            .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue))
        ),
        Arguments.of(Stream.of(
                new SimpleEntry<>(0, asList(
                    new OrderItem(MenuItem.SMALL_COFFEE, 2),
                    new OrderItem(MenuItem.MEDIUM_COFFEE, 2),
                    new OrderItem(MenuItem.EXTRA_MILK, 1),
                    new OrderItem(MenuItem.FOAMED_MILK, 1),
                    new OrderItem(MenuItem.ROASTED_COFFEE, 1))))
            .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue))
        ),
        Arguments.of(Stream.of(
                new SimpleEntry<>(0, asList(
                    new OrderItem(MenuItem.BACON_ROLL, 6),
                    new OrderItem(MenuItem.MEDIUM_COFFEE, 2))))
            .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue))
        )
    );
  }
}
