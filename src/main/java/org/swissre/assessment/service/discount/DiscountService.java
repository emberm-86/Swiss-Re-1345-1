package org.swissre.assessment.service.discount;

import java.util.List;
import org.swissre.assessment.domain.OrderItem;
import org.swissre.assessment.domain.datastructure.OrderMap;

public interface DiscountService {

  List<OrderItem> getDisOrdItems5thBev(Integer orderId, OrderMap allOrders);

  List<OrderItem> getDiscBev1Snack1(Integer orderId, OrderMap allOrders);
}
