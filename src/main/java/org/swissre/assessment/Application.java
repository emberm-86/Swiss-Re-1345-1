package org.swissre.assessment;

import java.util.Scanner;
import org.swissre.assessment.domain.MenuSelection;
import org.swissre.assessment.domain.MenuState;
import org.swissre.assessment.service.menu.MenuController;
import org.swissre.assessment.service.order.OrderService;
import org.swissre.assessment.service.order.OrderServiceImpl;
import org.swissre.assessment.service.util.MenuPrinter;

public class Application {

  public static void main(String[] args) {
    OrderService orderService = new OrderServiceImpl();
    MenuController menuController = new MenuController(orderService, new MenuSelection());

    MenuPrinter.printMainMenu();

    Scanner in = new Scanner(System.in);

    while (in.hasNext()) {
      MenuState menuState = menuController.getMenuSelection().getMenuSelected();
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
            menuController.createOrder();
            break;

          case LIST_ORDERS:
            menuController.backToMainMenu();
            break;
        }
        continue;
      }

      if ((menuCode.equalsIgnoreCase("c") && menuState == MenuState.CREATE_ORDER)) {
        System.out.println("You have cancelled your order, no worries! :)");
        menuController.backToMainMenu();
        continue;
      }

      try {
        menuController.launchSelectedMenu(menuCode);
      } catch (IllegalArgumentException e) {
        System.out.println(e.getMessage());
      }
    }
    in.close();
    System.out.println("Good bye! :)");
  }
}
