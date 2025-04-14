package org.swissre.assessment.service.order;

import java.util.ArrayList;
import java.util.List;
import org.swissre.assessment.domain.MenuItem;
import org.swissre.assessment.domain.OrderItem;
import org.swissre.assessment.domain.datastructure.OrderMap;

public class OrderServiceImpl implements OrderService {

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
      System.out.println("You have ordered nothing.");
      return;
    }

    ordStgProvider.addNewOrder(new ArrayList<>(ordSelectionCurrent));

    System.out.println("You can check your bill here.");

    singleOrderPrinterService.print(
        ordStgProvider.getLastOrderIndex(),
        ordSelectionCurrent,
        ordStgProvider.getAllOrders(),
        true);

    ordSelectionCurrent.clear();
  }

  @Override
  public void printAllOrders() {
    System.out.println("========================");

    OrderMap allOrders = ordStgProvider.getAllOrders();

    if (allOrders.isEmpty()) {
      System.out.println("There is no order in the system.");
    }

    allOrders.forEach(
        (orderId, order) -> {
          if (orderId > 0) {
            System.out.println();
          }

          System.out.println("Order: " + orderId);
          singleOrderPrinterService.print(orderId, order, allOrders, false);
        });
    System.out.println("========================\n");
  }
}
