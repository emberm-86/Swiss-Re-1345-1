package org.swissre.assessment;

import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.swissre.assessment.domain.MenuItem;
import org.swissre.assessment.domain.OrderItem;
import org.swissre.assessment.domain.datastructure.OrderMap;
import org.swissre.assessment.service.billing.BillingService;
import org.swissre.assessment.service.billing.BillingServiceImpl;
import org.swissre.assessment.service.discount.DiscountService;
import org.swissre.assessment.service.discount.DiscountServiceImpl;

public class DiscountBillingServiceTest {

  DiscountService discountService = new DiscountServiceImpl();
  BillingService billingService = new BillingServiceImpl();

  @ParameterizedTest
  @MethodSource("createOneComplexOrder")
  public void test5thBeverageInSameOrder(OrderMap orders) {
    List<OrderItem> disOrds5thBev = discountService.getDisOrdItems5thBev(orders.size() - 1, orders);

    assertEquals(2, disOrds5thBev.size());
    assertEquals(disOrds5thBev.get(0), new OrderItem(MenuItem.MEDIUM_COFFEE, 1));
    assertEquals(disOrds5thBev.get(1), new OrderItem(MenuItem.ORANGE_JUICE, 1));

    BigDecimal origSumPrice = billingService.calcSum(orders.get(0));
    BigDecimal discSumPrice = billingService.calcSumWithDisc(orders.get(0), disOrds5thBev);

    BigDecimal expectedDiff =
        disOrds5thBev.stream()
            .map(DiscountBillingServiceTest::getSumPrice)
            .reduce(ZERO, BigDecimal::add);

    assertEquals(origSumPrice, new BigDecimal("46.80"));
    assertEquals(expectedDiff, origSumPrice.subtract(discSumPrice));
  }

  @ParameterizedTest
  @MethodSource("createMultipleOrders")
  public void test5thBeverageInSameOrderMultipleOrders(OrderMap orders) {
    List<OrderItem> discOrders1 = discountService.getDisOrdItems5thBev(0, orders);
    List<OrderItem> discOrders2 = discountService.getDisOrdItems5thBev(1, orders);
    List<OrderItem> discOrders3 = discountService.getDisOrdItems5thBev(2, orders);

    assertEquals(1, discOrders1.size());
    assertEquals(1, discOrders2.size());
    assertEquals(2, discOrders3.size());

    assertEquals(discOrders1.get(0), new OrderItem(MenuItem.MEDIUM_COFFEE, 1));
    assertEquals(discOrders2.get(0), new OrderItem(MenuItem.ORANGE_JUICE, 1));
    assertEquals(discOrders3.get(0), new OrderItem(MenuItem.LARGE_COFFEE, 1));
    assertEquals(discOrders3.get(1), new OrderItem(MenuItem.MEDIUM_COFFEE, 1));

    for (int i = 0; i < orders.size(); i++) {
      List<OrderItem> disOrdItems5thBev = discountService.getDisOrdItems5thBev(i, orders);
      BigDecimal origSumPrice = billingService.calcSum(orders.get(i));
      BigDecimal discSumPrice = billingService.calcSumWithDisc(orders.get(i), disOrdItems5thBev);

      BigDecimal expectedDiff =
          disOrdItems5thBev.stream()
              .map(DiscountBillingServiceTest::getSumPrice)
              .reduce(ZERO, BigDecimal::add);

      assertEquals(expectedDiff, origSumPrice.subtract(discSumPrice));
    }
  }

  @ParameterizedTest
  @MethodSource("createOneComplexOrder")
  public void testBeverage1Snack1(OrderMap orders) {
    List<OrderItem> discountedBev1Snack1 = discountService.getDiscBev1Snack1(0, orders);

    assertEquals(2, discountedBev1Snack1.size());
    assertEquals(discountedBev1Snack1.get(0), new OrderItem(MenuItem.EXTRA_MILK, 1));
    assertEquals(discountedBev1Snack1.get(1), new OrderItem(MenuItem.FOAMED_MILK, 1));

    List<OrderItem> order1 = orders.get(0);
    BigDecimal origSumPrice = billingService.calcSum(order1);
    BigDecimal discSumPrice = billingService.calcSumWithDisc(order1, discountedBev1Snack1);

    BigDecimal expectedDiff =
        discountedBev1Snack1.stream()
            .map(DiscountBillingServiceTest::getSumPrice)
            .reduce(ZERO, BigDecimal::add);

    assertEquals(expectedDiff, origSumPrice.subtract(discSumPrice));
  }

  @ParameterizedTest
  @MethodSource("createOrdersNoDiscount")
  public void test5thBeverageInSameOrderNoDiscount(OrderMap orders) {
    List<OrderItem> discOrders5thBeverage = discountService.getDisOrdItems5thBev(0, orders);

    assertTrue(discOrders5thBeverage.isEmpty());

    List<OrderItem> order1 = orders.get(0);
    BigDecimal origSumPrice = billingService.calcSum(order1);
    BigDecimal discSumPrice = billingService.calcSumWithDisc(order1, discOrders5thBeverage);

    assertEquals(origSumPrice, discSumPrice);
  }

  @ParameterizedTest
  @MethodSource("createOrdersNoDiscount")
  public void testBeverage1Snack1NoDiscount(OrderMap orders) {
    List<OrderItem> discountedBeverage1Snack1 = discountService.getDiscBev1Snack1(0, orders);

    assertTrue(discountedBeverage1Snack1.isEmpty());

    List<OrderItem> order1 = orders.get(0);
    BigDecimal origSumPrice = billingService.calcSum(order1);
    BigDecimal discSumPrice = billingService.calcSumWithDisc(order1, discountedBeverage1Snack1);

    assertEquals(origSumPrice, discSumPrice);
  }

  private static Stream<Arguments> createOneComplexOrder() {
    return Stream.of(
        Arguments.of(
            Stream.of(
                    new SimpleEntry<>(
                        0,
                        Arrays.asList(
                            new OrderItem(MenuItem.BACON_ROLL, 2),
                            new OrderItem(MenuItem.SMALL_COFFEE, 1),
                            new OrderItem(MenuItem.ORANGE_JUICE, 2),
                            new OrderItem(MenuItem.MEDIUM_COFFEE, 3),
                            new OrderItem(MenuItem.ORANGE_JUICE, 4),
                            new OrderItem(MenuItem.ROASTED_COFFEE, 1),
                            new OrderItem(MenuItem.FOAMED_MILK, 1),
                            new OrderItem(MenuItem.EXTRA_MILK, 1),
                            new OrderItem(MenuItem.ROASTED_COFFEE, 1))))
                .collect(createOrderMap())));
  }

  private static Stream<Arguments> createMultipleOrders() {
    return Stream.of(
        Arguments.of(
            Stream.of(
                    new SimpleEntry<>(
                        0,
                        Arrays.asList(
                            new OrderItem(MenuItem.BACON_ROLL, 2),
                            new OrderItem(MenuItem.SMALL_COFFEE, 1),
                            new OrderItem(MenuItem.ORANGE_JUICE, 2),
                            new OrderItem(MenuItem.MEDIUM_COFFEE, 3),
                            new OrderItem(MenuItem.ORANGE_JUICE, 2),
                            new OrderItem(MenuItem.EXTRA_MILK, 1),
                            new OrderItem(MenuItem.FOAMED_MILK, 1),
                            new OrderItem(MenuItem.ROASTED_COFFEE, 1),
                            new OrderItem(MenuItem.EXTRA_MILK, 1))),
                    new SimpleEntry<>(
                        1,
                        Arrays.asList(
                            new OrderItem(MenuItem.ORANGE_JUICE, 3),
                            new OrderItem(MenuItem.BACON_ROLL, 4))),
                    new SimpleEntry<>(
                        2,
                        Arrays.asList(
                            new OrderItem(MenuItem.LARGE_COFFEE, 4),
                            new OrderItem(MenuItem.MEDIUM_COFFEE, 5))))
                .collect(createOrderMap())));
  }

  private static Stream<Arguments> createOrdersNoDiscount() {
    return Stream.of(
        Arguments.of(
            Stream.of(
                    new SimpleEntry<>(
                        0, Collections.singletonList(new OrderItem(MenuItem.ORANGE_JUICE, 4))))
                .collect(createOrderMap())),
        Arguments.of(
            Stream.of(
                    new SimpleEntry<>(
                        0,
                        Arrays.asList(
                            new OrderItem(MenuItem.SMALL_COFFEE, 2),
                            new OrderItem(MenuItem.MEDIUM_COFFEE, 2),
                            new OrderItem(MenuItem.EXTRA_MILK, 1),
                            new OrderItem(MenuItem.FOAMED_MILK, 1),
                            new OrderItem(MenuItem.ROASTED_COFFEE, 1))))
                .collect(createOrderMap())),
        Arguments.of(
            Stream.of(
                    new SimpleEntry<>(
                        0,
                        Arrays.asList(
                            new OrderItem(MenuItem.BACON_ROLL, 6),
                            new OrderItem(MenuItem.MEDIUM_COFFEE, 2))))
                .collect(createOrderMap())));
  }

  private static Collector<SimpleEntry<Integer, List<OrderItem>>, ?, OrderMap> createOrderMap() {
    return Collectors.toMap(
            SimpleEntry::getKey, SimpleEntry::getValue, (e1, e2) -> e1, OrderMap::new);
  }

  private static BigDecimal getSumPrice(OrderItem orderItem) {
    BigDecimal price = orderItem.getMenuItem().getPrice();
    BigDecimal quantity = new BigDecimal(String.valueOf(orderItem.getQuantity()));

    return price.multiply(quantity);
  }
}
