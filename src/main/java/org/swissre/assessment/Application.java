package org.swissre.assessment;

import static org.swissre.assessment.service.menu.MenuUtil.checkSelectableExtras;
import static org.swissre.assessment.service.menu.MenuUtil.launchSelectedMenu;
import static org.swissre.assessment.service.menu.MenuUtil.printMainMenu;

import java.util.List;
import java.util.Scanner;
import org.swissre.assessment.domain.MenuItem;
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
      MenuItem menuItemSelected = menuSelection.getMenuItemSelected();
      List<MenuItem> selectedExtras = menuSelection.getSelectedExtras();
      boolean extraAlreadyChosen = menuSelection.isExtraAlreadyChosen();

      String[] arguments = in.nextLine().split("\\s+");
      String menuCode = arguments[0];

      if ((menuCode.equalsIgnoreCase("Q")
          || (menuCode.equals("3")) && menuState == MenuState.MAIN_MENU)) {
        break;
      }

      if (menuCode.equals("")) {
        continue;
      }

      if (menuCode.equalsIgnoreCase("x")) {
        switch (menuState) {
          case LIST_ORDERS:
            menuSelection.setMenuSelected(MenuState.MAIN_MENU);
            printMainMenu();
            break;

          case CREATE_ORDER:
            if (menuItemSelected != null && menuItemSelected.isCoffee() && !extraAlreadyChosen) {
              System.out.println(
                  "You can choose an extra with codes to your coffee product: "
                      + checkSelectableExtras(selectedExtras) + " or say no(n)!");
            } else {
              orderService.closeOrder();
              menuSelection.setMenuSelected(MenuState.MAIN_MENU);
              printMainMenu();
            }
            break;
        }
        continue;
      }

      if ((menuCode.equalsIgnoreCase("c") && menuState == MenuState.CREATE_ORDER)) {
        menuSelection.setMenuSelected(MenuState.MAIN_MENU);
        System.out.println("You have cancelled your order, no worries! :)");
        printMainMenu();
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