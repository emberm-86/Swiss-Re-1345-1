package org.swissre.assessment.service.billing;

import java.util.List;
import org.swissre.assessment.domain.OrderItem;

public interface BillingService {

  float calculateSum(List<OrderItem> order);

  float calculateSumWithDiscounts(List<OrderItem> order, List<OrderItem> discountedOrderItems);
}
