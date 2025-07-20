package org.swissre.assessment.service.order;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.swissre.assessment.domain.MenuItem;
import org.swissre.assessment.domain.OrderItem;
import org.swissre.assessment.domain.datastructure.OrderMap;

public class OrderServiceImpl implements OrderService {

  private static final Logger LOGGER = LogManager.getLogger(OrderServiceImpl.class);

  List<OrderItem> ordSelectionCurrent = new ArrayList<>();
  OrderStorageProvider ordStgProvider = new OrderStorageProviderImpl();
  SingleOrderPrinterService singleOrderPrinterService = new SingleOrderPrinterServiceImpl();

  @Override
  public void addNewOrderItem(MenuItem menuItemSelected, String menuCode) {
    ordSelectionCurrent.add(new OrderItem(menuItemSelected, Integer.parseInt(menuCode)));
  }

  @Override
  public void closeOrder() {
    if (ordSelectionCurrent.isEmpty()) {
      LOGGER.info("You have ordered nothing.");
      return;
    }

    ordStgProvider.addNewOrder(new ArrayList<>(ordSelectionCurrent));

    LOGGER.info("You can check your bill here.");

    singleOrderPrinterService.print(
        ordStgProvider.getLastOrderIndex(),
        ordSelectionCurrent,
        ordStgProvider.getAllOrders(),
        true);

    ordSelectionCurrent.clear();
  }

  @Override
  public void printAllOrders() {
    LOGGER.info("========================");

    OrderMap allOrders = ordStgProvider.getAllOrders();

    if (allOrders.isEmpty()) {
      LOGGER.info("There is no order in the system.");
    }

    allOrders.forEach(
        (orderId, order) -> {
          if (orderId > 0) {
            LOGGER.info("");
          }

          LOGGER.info("Order: {}", orderId);
          singleOrderPrinterService.print(orderId, order, allOrders, false);
        });
    LOGGER.info("========================\n");
  }
}
