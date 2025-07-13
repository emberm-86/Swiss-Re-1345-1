package org.swissre.assessment.service.order;

import org.swissre.assessment.domain.OrderItem;
import org.swissre.assessment.domain.datastructure.OrderMap;

import java.util.List;

public interface SingleOrderPrinterService {
  void print(Integer orderId, List<OrderItem> order, OrderMap allOrders, boolean receipt);
}
