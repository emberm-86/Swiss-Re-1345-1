package org.swissre.assessment.service.order;

import java.util.List;
import java.util.Map;
import org.swissre.assessment.domain.MenuItem;
import org.swissre.assessment.domain.OrderItem;

public interface OrderService {

  void addNewOrder(MenuItem menuItemSelected, String menuCode);

  Map<Integer, List<OrderItem>> getDiscountedOrders5thBeverage(Map<Integer, List<OrderItem>> allOrders);
  void closeOrder();

  List<OrderItem> getDiscountsBeverage1Snack1(List<OrderItem> order);

  void printAllOrders();

}
