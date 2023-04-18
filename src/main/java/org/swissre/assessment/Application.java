package org.swissre.assessment;

import static org.swissre.assessment.domain.MenuItem.checkIfExtraByCode;
import static org.swissre.assessment.service.Util.isValidNum;
import static org.swissre.assessment.service.Util.printCreateOrderMenu;
import static org.swissre.assessment.service.Util.printMainMenu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import org.swissre.assessment.domain.MenuItem;
import org.swissre.assessment.domain.MenuState;
import org.swissre.assessment.service.order.OrderService;
import org.swissre.assessment.service.order.OrderServiceImpl;

public class Application {

  private static MenuState MENU_SELECTED = MenuState.MAIN_MENU;
  private static MenuItem menuItemSelected;
  private static final List<MenuItem> selectedExtras = new ArrayList<>();

  private static boolean extraAlreadyChosen = false;

  private static OrderService orderService;

  public static void main(String[] args) {
    orderService = new OrderServiceImpl();

    printMainMenu();

    Scanner in = new Scanner(System.in);

    while (in.hasNext()) {
      String[] arguments = in.nextLine().split("\\s+");
      String menuCode = arguments[0];

      if ((menuCode.equalsIgnoreCase("Q")
          || (menuCode.equals("3")) && MENU_SELECTED == MenuState.MAIN_MENU)) {
        break;
      }

      if (menuCode.equals("")) {
        continue;
      }

      if (menuCode.equalsIgnoreCase("x")) {
        switch (MENU_SELECTED) {
          case LIST_ORDERS:
            MENU_SELECTED = MenuState.MAIN_MENU;
            printMainMenu();
            break;

          case CREATE_ORDER:
            if (menuItemSelected != null && menuItemSelected.isCoffee() && !extraAlreadyChosen) {
              System.out.println(
                  "You can choose an extra with codes to your coffee product: " + checkSelectableExtras()
                      + " or say no(n)!");
            } else {
              orderService.closeOrder();
              MENU_SELECTED = MenuState.MAIN_MENU;
              printMainMenu();
            }
            break;
        }
        continue;
      }

      if ((menuCode.equalsIgnoreCase("c") && MENU_SELECTED == MenuState.CREATE_ORDER)) {
        MENU_SELECTED = MenuState.MAIN_MENU;
        System.out.println("You have cancelled your order, no worries! :) ");
        printMainMenu();
        continue;
      }

      try {
        launchSelectedMenu(menuCode);
      } catch (IllegalArgumentException e) {
        System.out.println(e.getMessage());
      }
    }
    in.close();
    System.out.println("Good bye! :)");
  }

  public static void launchSelectedMenu(String menuCode) {
    if (MENU_SELECTED == MenuState.CREATE_ORDER) {
      launchCreateOrderMenu(menuCode);
    } else if (MENU_SELECTED == MenuState.MAIN_MENU) {
      launchMainMenu(menuCode);
    }
  }

  public static void launchMainMenu(String menuCode) {
    switch (menuCode) {
      case "1":
        MENU_SELECTED = MenuState.CREATE_ORDER;
        printCreateOrderMenu();
        break;
      case "2":
        MENU_SELECTED = MenuState.LIST_ORDERS;
        orderService.printAllOrders();
        System.out.println("Press X to return to the Main Menu!");
        break;
      default:
        break;
    }
  }

  public static void launchCreateOrderMenu(String menuCode) {
    if (menuItemSelected == null) {

      if (Arrays.stream(MenuItem.codes())
          .noneMatch(menuItemCode -> menuItemCode.equals(menuCode))) {
        throw new IllegalArgumentException("You chose an invalid product code, please retry it!");
      }

      if (checkIfExtraByCode(menuCode)) {
        throw new IllegalArgumentException(
            "Please choose a coffee product before selecting any extras: sc, mc, lc!");
      }

      menuItemSelected = MenuItem.getMenuItemByCode(menuCode);

      if (MenuItem.isCoffee(menuCode)) {
        System.out.println(
            "You can choose an extra with codes to your coffee product: " + checkSelectableExtras()
                + " or say no(n)!");
      } else {
        System.out.println("Please type the quantity: ");
      }
    } else {
      if (!(menuItemSelected.isCoffee() && (menuCode.equalsIgnoreCase("n")
          || menuCode.equalsIgnoreCase("no")))) {
        if (menuItemSelected.isCoffee() && MenuItem.checkIfExtraByCode(menuCode)) {
          MenuItem extra = MenuItem.getMenuItemByCode(menuCode);
          if (!selectedExtras.contains(extra)) {
            selectedExtras.add(MenuItem.getMenuItemByCode(menuCode));
            if (extraAlreadyChosen) {
              System.out.println("Please type the quantity: ");
            } else {
              String selectableExtras = checkSelectableExtras();
              if (!selectableExtras.isEmpty()) {
                System.out.println(
                    "You can choose an other coffee extra with valid code: " + selectableExtras
                        + " or say no(n)!");
              } else {
                System.out.println(
                    "No selectable extras left, please type the quantity:");
                extraAlreadyChosen = true;
              }
            }
          } else {
            String selectableExtras = checkSelectableExtras();
            if (!selectableExtras.isEmpty()) {
              System.out.println(
                  "This has already been chosen: " + extra.getCode() + ". Choose an other one " +
                      selectableExtras + " or say no(n)!");
            } else {
              System.out.println("No selectable extras left, please type the quantity:");
              extraAlreadyChosen = true;
            }
          }
        } else {
          if (menuItemSelected.isCoffee() && !(checkIfExtraByCode(menuCode)
              || extraAlreadyChosen)) {
            System.out.println(
                "Please choose a coffee with valid extra code: " + checkSelectableExtras()
                    + " or say no(n)!");
          } else {
            if (!isValidNum(menuCode)) {
              System.out.println("Please give a valid number: > 0 as an input!");
            } else {
              orderService.addNewOrder(menuItemSelected, menuCode);

              selectedExtras.forEach(
                  extraSelected -> orderService.addNewOrder(extraSelected, menuCode));
              selectedExtras.clear();

              menuItemSelected = null;
              extraAlreadyChosen = false;

              System.out.println(
                  "Please choose another product with the code(second column) or submit your order(x) or cancel(c): ");
            }
          }
        }
      } else {
        extraAlreadyChosen = true;
        System.out.println("Please type the quantity: ");
      }
    }
  }

  private static String checkSelectableExtras() {
    return MenuItem.extras().stream()
        .filter(menuItemExtra -> !selectedExtras.contains(menuItemExtra))
        .map(MenuItem::getCode)
        .collect(Collectors.joining(", "));
  }
}