package org.swissre.assessment.service.billing;

import java.math.BigDecimal;
import java.util.List;
import org.swissre.assessment.domain.OrderItem;

public interface BillingService {

  BigDecimal calcSum(List<OrderItem> order);

  BigDecimal calcSumWithDisc(List<OrderItem> order, List<OrderItem> discountedOrderItems);
}
