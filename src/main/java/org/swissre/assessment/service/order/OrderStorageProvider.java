package org.swissre.assessment.service.order;

import java.util.List;
import java.util.Map;
import org.swissre.assessment.domain.OrderItem;

public interface OrderStorageProvider {

  Map<Integer, List<OrderItem>> getAllOrders();

  void addNewOrder(List<OrderItem> order);
  
  int getLastOrderIndex();
}
