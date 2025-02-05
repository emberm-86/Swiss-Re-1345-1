package org.swissre.assessment;

import static org.swissre.assessment.service.menu.MenuPrinter.printMainMenu;
import static org.swissre.assessment.service.menu.MenuUtil.backToMainMenu;
import static org.swissre.assessment.service.menu.MenuUtil.createOrder;
import static org.swissre.assessment.service.menu.MenuUtil.launchSelectedMenu;

import java.util.Scanner;
import org.swissre.assessment.domain.MenuSelection;
import org.swissre.assessment.domain.MenuState;
import org.swissre.assessment.service.order.OrderService;
import org.swissre.assessment.service.order.OrderServiceImpl;

public class Application {

  public static void main(String[] args) {
    OrderService orderService = new OrderServiceImpl();
    MenuSelection menuSelection = new MenuSelection();

    printMainMenu();

    Scanner in = new Scanner(System.in);

    while (in.hasNext()) {
      MenuState menuState = menuSelection.getMenuSelected();
      String[] arguments = in.nextLine().split("\\s+");

      if (arguments.length == 0) {
        continue;
      }

      String menuCode = arguments[0];

      if ((menuCode.equalsIgnoreCase("q")
          || (menuCode.equals("3")) && menuState == MenuState.MAIN_MENU)) {
        break;
      }

      if (menuCode.isEmpty()) {
        continue;
      }

      if (menuCode.equalsIgnoreCase("x")) {
        switch (menuState) {
          case CREATE_ORDER:
            createOrder(orderService, menuSelection);
            break;

          case LIST_ORDERS:
            backToMainMenu(menuSelection);
            break;
        }
        continue;
      }

      if ((menuCode.equalsIgnoreCase("c") && menuState == MenuState.CREATE_ORDER)) {
        System.out.println("You have cancelled your order, no worries! :)");
        backToMainMenu(menuSelection);
        continue;
      }

      try {
        launchSelectedMenu(menuCode, menuSelection, orderService);
      } catch (IllegalArgumentException e) {
        System.out.println(e.getMessage());
      }
    }
    in.close();
    System.out.println("Good bye! :)");
  }
}
