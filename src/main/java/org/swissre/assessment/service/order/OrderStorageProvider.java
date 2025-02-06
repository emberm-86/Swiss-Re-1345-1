package org.swissre.assessment.service.order;

import java.util.List;
import org.swissre.assessment.domain.OrderItem;
import org.swissre.assessment.domain.datastructure.OrderMap;

public interface OrderStorageProvider {

  OrderMap getAllOrders();

  void addNewOrder(List<OrderItem> order);
  
  int getLastOrderIndex();
}
