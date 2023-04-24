package org.swissre.assessment.service.order;

import org.swissre.assessment.domain.MenuItem;

public interface OrderService {

  void addNewOrder(MenuItem menuItemSelected, String menuCode);

  void closeOrder();

  void printAllOrders();
}
