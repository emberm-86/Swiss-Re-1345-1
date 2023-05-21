package org.swissre.assessment.service.menu;

import static org.swissre.assessment.domain.MenuItem.checkIfExtraByCode;
import static org.swissre.assessment.service.menu.MenuPrinter.printCreateOrderMenu;
import static org.swissre.assessment.service.menu.MenuPrinter.printMainMenu;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.swissre.assessment.domain.MenuItem;
import org.swissre.assessment.domain.MenuSelection;
import org.swissre.assessment.domain.MenuState;
import org.swissre.assessment.service.order.OrderService;

public class MenuUtil {

  public static void launchSelectedMenu(String menuCode, MenuSelection menuSelection,
      OrderService orderService) {

    if (menuSelection.getMenuSelected() == MenuState.CREATE_ORDER) {
      launchCreateOrderMenu(menuCode, menuSelection, orderService);
    } else if (menuSelection.getMenuSelected() == MenuState.MAIN_MENU) {
      launchMainMenu(menuCode, menuSelection, orderService);
    }
  }

  public static void launchMainMenu(String menuCode, MenuSelection menuSelection,
      OrderService orderService) {

    switch (menuCode) {
      case "1":
        menuSelection.setMenuSelected(MenuState.CREATE_ORDER);
        printCreateOrderMenu();
        break;

      case "2":
        menuSelection.setMenuSelected(MenuState.LIST_ORDERS);
        orderService.printAllOrders();
        System.out.println("Press X to return to the Main Menu!");
        break;

      default:
        break;
    }
  }

  public static void backToMainMenu(MenuSelection menuSelection) {
    menuSelection.setMenuSelected(MenuState.MAIN_MENU);
    printMainMenu();
  }

  public static void launchCreateOrderMenu(String menuCode, MenuSelection menuSelection,
      OrderService orderService) {

    MenuItem menuItemSelected = menuSelection.getMenuItemSelected();
    List<MenuItem> selectedExtras = menuSelection.getSelectedExtras();

    if (menuItemSelected == null) {

      if (Arrays.stream(MenuItem.codes()).noneMatch(menuCode::equals)) {
        throw new IllegalArgumentException("You chose an invalid product code, please retry it!");
      }

      if (checkIfExtraByCode(menuCode)) {
        throw new IllegalArgumentException(
            "Please choose a coffee product before selecting any extras: sc, mc, lc!");
      }

      menuSelection.setMenuItemSelected(MenuItem.getMenuItemByCode(menuCode));

      if (MenuItem.isCoffee(menuCode)) {
        System.out.println(
            "You can choose an extra with a code to add it to your coffee product: "
                + checkSelectableExtras(selectedExtras) + " or say no(n)!");
      } else {
        System.out.println("Please type the quantity:");
      }
    } else {
      addMenuItem(menuCode, menuSelection, orderService);
    }
  }

  private static void addMenuItem(String menuCode, MenuSelection menuSelection,
      OrderService orderService) {

    MenuItem menuItemSelected = menuSelection.getMenuItemSelected();
    Stream<String> noOptions = Stream.of("n", "no");

    if (menuItemSelected.isCoffee() && noOptions.anyMatch(menuCode::equalsIgnoreCase)) {
      if (menuSelection.isExtraSelectionDone()) {
        if (!isValidNum(menuCode)) {
          System.out.println("Please give a valid number: > 0 as an input!");
        }
      } else {
        System.out.println("Please type the quantity:");
      }
      menuSelection.setExtraSelectionDone(true);
    } else {
      addMenuItemWithExtraCheck(menuCode, menuSelection, orderService);
    }
  }

  private static void addMenuItemWithExtraCheck(String menuCode, MenuSelection menuSelection,
      OrderService orderService) {

    boolean extraSelectionDone = menuSelection.isExtraSelectionDone();
    MenuItem menuItemSelected = menuSelection.getMenuItemSelected();
    List<MenuItem> selectedExtras = menuSelection.getSelectedExtras();

    if (menuItemSelected.isCoffee()) {
      if (MenuItem.checkIfExtraByCode(menuCode)) {
        if (extraSelectionDone) {
          System.out.println("Please give a valid number: > 0 as an input!");
        } else {
          addExtraMenuItem(menuSelection, MenuItem.getMenuItemByCode(menuCode));
        }
      } else if (!extraSelectionDone) {
        System.out.println(
            "Please choose the coffee with valid extra code: " +
                checkSelectableExtras(selectedExtras) + " or say no(n)!");
      } else {
        addNonExtraMenuItem(menuCode, menuSelection, orderService);
      }
    } else {
      addNonExtraMenuItem(menuCode, menuSelection, orderService);
    }
  }

  private static void addNonExtraMenuItem(String menuCode, MenuSelection menuSelection,
      OrderService orderService) {

    if (!isValidNum(menuCode)) {
      System.out.println("Please give a valid number: > 0 as an input!");
    } else {
      addNewOrderItemWithExtras(menuCode, menuSelection, orderService);
    }
  }

  private static void addExtraMenuItem(MenuSelection menuSelection, MenuItem extra) {
    List<MenuItem> selectedExtras = menuSelection.getSelectedExtras();

    if (!selectedExtras.contains(extra)) {
      selectedExtras.add(extra);
      applySelectableCheck(menuSelection, null);
    } else {
      applySelectableCheck(menuSelection, extra);
    }
  }

  private static void addNewOrderItemWithExtras(String menuCode, MenuSelection menuSelection,
      OrderService orderService) {

    MenuItem menuItemSelected = menuSelection.getMenuItemSelected();
    List<MenuItem> selectedExtras = menuSelection.getSelectedExtras();

    orderService.addNewOrderItem(menuItemSelected, menuCode);

    selectedExtras.forEach(
        extraSelected -> orderService.addNewOrderItem(extraSelected, menuCode));
    selectedExtras.clear();

    menuSelection.setMenuItemSelected(null);
    menuSelection.setExtraSelectionDone(false);

    System.out.println(
        "Please choose a product with the code(second column) or submit(x) or cancel(c) your order:");
  }

  private static void applySelectableCheck(MenuSelection menuSelection, MenuItem extra) {
    List<MenuItem> selectedExtras = menuSelection.getSelectedExtras();
    String selectableExtras = checkSelectableExtras(selectedExtras);

    if (!selectableExtras.isEmpty()) {
      if (extra == null) {
        System.out.println(
            "You can choose another coffee extra with valid code: " + selectableExtras
                + " or say no(n)!");
      } else {
        System.out.println(
            "'" + extra.getCode() + "'" + " has already been chosen."
                + " Please choose another one: " + selectableExtras + " or say no(n)!");
      }
    } else {
      System.out.println("No selectable extras left, please type the quantity:");
      menuSelection.setExtraSelectionDone(true);
    }
  }

  public static void createOrder(OrderService orderService, MenuSelection menuSelection) {
    MenuItem menuItemSelected = menuSelection.getMenuItemSelected();
    List<MenuItem> selectedExtras = menuSelection.getSelectedExtras();
    boolean extraSelectionDone = menuSelection.isExtraSelectionDone();

    if (menuItemSelected != null && menuItemSelected.isCoffee() && !extraSelectionDone) {
      System.out.println(
          "You can choose an extra with a code to add it to your coffee product: "
              + checkSelectableExtras(selectedExtras) + " or say no(n)!");
    } else {
      orderService.closeOrder();
      backToMainMenu(menuSelection);
    }
  }

  private static String checkSelectableExtras(List<MenuItem> selectedExtras) {
    return MenuItem.extras().stream()
        .filter(menuItemExtra -> !selectedExtras.contains(menuItemExtra))
        .map(MenuItem::getCode)
        .collect(Collectors.joining(", "));
  }

  private static boolean isValidNum(String strNum) {
    if (strNum == null) {
      return false;
    }
    try {
      return Integer.parseInt(strNum) > 0;
    } catch (NumberFormatException nfe) {
      return false;
    }
  }
}
