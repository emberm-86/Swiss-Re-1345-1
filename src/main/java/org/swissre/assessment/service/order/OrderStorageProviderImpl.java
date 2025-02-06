package org.swissre.assessment.service.order;

import java.util.List;
import org.swissre.assessment.domain.OrderItem;
import org.swissre.assessment.domain.datastructure.OrderMap;

public class OrderStorageProviderImpl implements OrderStorageProvider {

  private static final OrderMap INTERNAL_MAP = new OrderMap();

  @Override
  public OrderMap getAllOrders() {
    return INTERNAL_MAP;
  }

  @Override
  public void addNewOrder(List<OrderItem> order) {
    INTERNAL_MAP.put(INTERNAL_MAP.values().size(), order);
  }

  @Override
  public int getLastOrderIndex() {
    return INTERNAL_MAP.values().size() - 1;
  }
}
