package org.swissre.assessment.service.discount;

import java.util.List;
import org.swissre.assessment.domain.OrderItem;
import org.swissre.assessment.domain.datastructure.OrderMap;

public interface DiscountService {
  List<OrderItem> getDiscountedOrderItems(Integer orderId, OrderMap allOrders);
}
