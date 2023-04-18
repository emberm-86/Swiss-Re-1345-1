package org.swissre.assessment.service.order;

import java.util.List;
import java.util.Map;
import org.swissre.assessment.domain.MenuItem;
import org.swissre.assessment.domain.OrderItem;

public interface OrderService {

  void addNewOrder(MenuItem menuItemSelected, String menuCode);
  List<OrderItem> getDisOrdItems5thBev(Integer orderId, Map<Integer, List<OrderItem>> allOrders);
  List<OrderItem> getDiscountsBeverage1Snack1(Integer orderId, Map<Integer, List<OrderItem>> allOrders);
  void closeOrder();
  void printAllOrders();
}
