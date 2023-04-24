package org.swissre.assessment.service.discount;

import java.util.List;
import java.util.Map;
import org.swissre.assessment.domain.OrderItem;

public interface DiscountService {

  List<OrderItem> getDisOrdItems5thBev(Integer orderId, Map<Integer, List<OrderItem>> allOrders);

  List<OrderItem> getDiscBevSnack1(Integer orderId, Map<Integer, List<OrderItem>> allOrders);
}
