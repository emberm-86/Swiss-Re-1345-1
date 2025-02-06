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

  public static void launchSelectedMenu(String mCode, MenuSelection menuSlc, OrderService ordSrv) {
    if (menuSlc.getMenuSelected() == MenuState.CREATE_ORDER) {
      launchCreateOrdMenu(mCode, menuSlc, ordSrv);
    } else if (menuSlc.getMenuSelected() == MenuState.MAIN_MENU) {
      launchMainMenu(mCode, menuSlc, ordSrv);
    }
  }

  public static void launchMainMenu(String menuCode, MenuSelection menuSlc, OrderService orderSrv) {
    switch (menuCode) {
      case "1":
        menuSlc.setMenuSelected(MenuState.CREATE_ORDER);
        printCreateOrderMenu();
        break;

      case "2":
        menuSlc.setMenuSelected(MenuState.LIST_ORDERS);
        orderSrv.printAllOrders();
        System.out.println("Press X to return to the Main Menu!");
        break;

      default:
        break;
    }
  }

  public static void backToMainMenu(MenuSelection menuSlc) {
    menuSlc.setMenuSelected(MenuState.MAIN_MENU);
    printMainMenu();
  }

  public static void launchCreateOrdMenu(String mCode, MenuSelection menuSlc, OrderService ordSrv) {
    MenuItem menuItemSelected = menuSlc.getMenuItemSelected();
    List<MenuItem> selectedExtras = menuSlc.getSelectedExtras();

    if (menuItemSelected == null) {
      if (Arrays.stream(MenuItem.codes()).noneMatch(mCode::equals)) {
        throw new IllegalArgumentException("You chose an invalid product code, please retry it!");
      }

      if (checkIfExtraByCode(mCode)) {
        throw new IllegalArgumentException(
            "Please choose a coffee product before selecting any extras: sc, mc, lc!");
      }

      menuSlc.setMenuItemSelected(MenuItem.getMenuItemByCode(mCode));

      if (MenuItem.isCoffee(mCode)) {
        System.out.println(
            "You can choose an extra with a code to add it to your coffee product: "
                + checkSelectableExtras(selectedExtras)
                + " or say no(n)!");
      } else {
        System.out.println("Please type the quantity:");
      }
    } else {
      addMenuItem(mCode, menuSlc, ordSrv);
    }
  }

  private static void addMenuItem(String menuCode, MenuSelection menuSlc, OrderService orderSrv) {
    MenuItem menuItemSelected = menuSlc.getMenuItemSelected();
    Stream<String> noOptions = Stream.of("n", "no");

    if (menuItemSelected.isCoffee() && noOptions.anyMatch(menuCode::equalsIgnoreCase)) {
      if (menuSlc.isExtraSelectionDone()) {
        System.out.println("Please give a valid number: > 0 as an input!");
      } else {
        System.out.println("Please type the quantity:");
      }
      menuSlc.setExtraSelectionDone(true);
    } else {
      addMenuItDoExtCheck(menuCode, menuSlc, orderSrv);
    }
  }

  private static void addMenuItDoExtCheck(String mCode, MenuSelection menuSlc, OrderService orSrv) {
    boolean extraSelectionDone = menuSlc.isExtraSelectionDone();
    MenuItem menuItemSelected = menuSlc.getMenuItemSelected();
    List<MenuItem> selectedExtras = menuSlc.getSelectedExtras();

    if (!menuItemSelected.isCoffee()) {
      addNonExtMenuItem(mCode, menuSlc, orSrv);
    } else if (extraSelectionDone) {
      if (MenuItem.checkIfExtraByCode(mCode)) {
        System.out.println("Please give a valid number: > 0 as an input!");
      } else {
        addNonExtMenuItem(mCode, menuSlc, orSrv);
      }
    } else if (MenuItem.checkIfExtraByCode(mCode)) {
      addExtraMenuItem(menuSlc, MenuItem.getMenuItemByCode(mCode));
    } else {
      System.out.println(
          "Please choose the coffee with a valid extra code: "
              + checkSelectableExtras(selectedExtras)
              + " or say no(n)!");
    }
  }

  private static void addNonExtMenuItem(String mCode, MenuSelection menuSlc, OrderService ordSrv) {
    if (!isValidNum(mCode)) {
      System.out.println("Please give a valid number: > 0 as an input!");
    } else {
      addNewOrdItAndExt(mCode, menuSlc, ordSrv);
    }
  }

  private static void addExtraMenuItem(MenuSelection menuSelection, MenuItem extra) {
    List<MenuItem> selectedExtras = menuSelection.getSelectedExtras();
    boolean extraIsIn = selectedExtras.contains(extra);

    if (!extraIsIn) {
      selectedExtras.add(extra);
    }
    applySelectableCheck(menuSelection, extraIsIn ? extra : null);
  }

  private static void addNewOrdItAndExt(String menuCode, MenuSelection menuSlc, OrderService oSrv) {
    MenuItem menuItemSelected = menuSlc.getMenuItemSelected();
    List<MenuItem> selectedExtras = menuSlc.getSelectedExtras();

    oSrv.addNewOrderItem(menuItemSelected, menuCode);

    selectedExtras.forEach(extraSelected -> oSrv.addNewOrderItem(extraSelected, menuCode));
    selectedExtras.clear();

    menuSlc.setMenuItemSelected(null);
    menuSlc.setExtraSelectionDone(false);

    System.out.println("Please choose a product with the code or submit(x), cancel(c) your order:");
  }

  private static void applySelectableCheck(MenuSelection menuSelection, MenuItem extra) {
    List<MenuItem> selectedExtras = menuSelection.getSelectedExtras();
    String selectableExtras = checkSelectableExtras(selectedExtras);

    if (selectableExtras.isEmpty()) {
      System.out.println("No selectable extras left, please type the quantity:");
      menuSelection.setExtraSelectionDone(true);
    } else if (extra == null) {
      System.out.println(
          "You can choose another coffee extra with valid code: "
              + selectableExtras
              + " or say no(n)!");
    } else {
      System.out.println(
          "'"
              + extra.getCode()
              + "'"
              + " has already been chosen."
              + " Please choose another one: "
              + selectableExtras
              + " or say no(n)!");
    }
  }

  public static void createOrder(OrderService orderService, MenuSelection menuSelection) {
    MenuItem menuItemSelected = menuSelection.getMenuItemSelected();
    List<MenuItem> selectedExtras = menuSelection.getSelectedExtras();
    boolean extraSelectionDone = menuSelection.isExtraSelectionDone();

    if (menuItemSelected != null && menuItemSelected.isCoffee() && !extraSelectionDone) {
      System.out.println(
          "You can choose an extra with a code to add it to your coffee product: "
              + checkSelectableExtras(selectedExtras)
              + " or say no(n)!");
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
