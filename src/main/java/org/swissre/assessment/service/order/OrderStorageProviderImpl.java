package org.swissre.assessment.service.order;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.swissre.assessment.domain.OrderItem;

public class OrderStorageProviderImpl implements OrderStorageProvider {

  private static final Map<Integer, List<OrderItem>> INTERNAL_MAP = new LinkedHashMap<>();

  @Override
  public Map<Integer, List<OrderItem>> getAllOrders() {
    return INTERNAL_MAP;
  }

  @Override
  public void addNewOrder(List<OrderItem> order) {
    INTERNAL_MAP.put(INTERNAL_MAP.values().size(), order);
  }

  @Override
  public Integer getLastOrderIndex() {
    return INTERNAL_MAP.values().size() - 1;
  }
}
