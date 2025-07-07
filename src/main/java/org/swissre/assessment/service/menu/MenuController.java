package org.swissre.assessment.service.menu;

import org.swissre.assessment.domain.MenuItem;
import org.swissre.assessment.domain.MenuSelection;
import org.swissre.assessment.domain.MenuState;
import org.swissre.assessment.service.order.OrderService;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.swissre.assessment.domain.MenuItem.checkIfExtraByCode;
import static org.swissre.assessment.service.util.MenuPrinter.printCreateOrderMenu;
import static org.swissre.assessment.service.util.MenuPrinter.printMainMenu;

public class MenuController {

  private final OrderService orderService;
  private final MenuSelection menuSelection;

  public MenuController(OrderService orderService, MenuSelection menuSelection) {
    this.orderService = orderService;
    this.menuSelection = menuSelection;
  }

  public boolean checkExit(String menuCode) {
    return ((menuCode.equalsIgnoreCase("q")
        || (menuCode.equals("3")) && menuSelection.getMenuSelected() == MenuState.MAIN_MENU));
  }

  public boolean preCheckMenuCode(String menuCode) {
    return menuCode.isEmpty() || keyXPressed(menuCode) || orderCancelled(menuCode);
  }

  private boolean keyXPressed(String menuCode) {
    if (!menuCode.equalsIgnoreCase("x")
        || (menuSelection.getMenuSelected() == MenuState.CREATE_ORDER
            && menuSelection.getMenuItemSelected() != null)) {
      return false;
    }

    switch (menuSelection.getMenuSelected()) {
      case CREATE_ORDER -> createOrder();
      case LIST_ORDERS -> backToMainMenu();
    }
    return true;
  }

  private boolean orderCancelled(String menuCode) {
    MenuState menuSelected = menuSelection.getMenuSelected();

    if ((!menuCode.equalsIgnoreCase("c") || menuSelected != MenuState.CREATE_ORDER)) {
      return false;
    }
    System.out.println("You have cancelled your order, no worries! :)");
    backToMainMenu();
    return true;
  }

  public void launchSelectedMenu(String menuCode) {
    if (menuSelection.getMenuSelected() == MenuState.CREATE_ORDER) {
      launchCreateOrderMenu(menuCode);
    } else if (menuSelection.getMenuSelected() == MenuState.MAIN_MENU) {
      launchMainMenu(menuCode);
    }
  }

  public void launchMainMenu(String menuCode) {
    switch (menuCode) {
      case "1" -> {
        menuSelection.setMenuSelected(MenuState.CREATE_ORDER);
        printCreateOrderMenu();
      }
      case "2" -> {
        menuSelection.setMenuSelected(MenuState.LIST_ORDERS);
        orderService.printAllOrders();
        System.out.println("Press X to return to the Main Menu!");
      }
    }
  }

  public void backToMainMenu() {
    menuSelection.setMenuSelected(MenuState.MAIN_MENU);
    printMainMenu();
  }

  public void launchCreateOrderMenu(String menuCode) {
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
            "You can choose an extra with it's code for your coffee product: "
                + checkSelectableExtras(selectedExtras)
                + " or say no(n)!");
      } else {
        System.out.println("Please type the quantity:");
      }
    } else {
      addMenuItem(menuCode);
    }
  }

  public void createOrder() {
    MenuItem menuItemSelected = menuSelection.getMenuItemSelected();
    List<MenuItem> selectedExtras = menuSelection.getSelectedExtras();
    boolean extraSelectionDone = menuSelection.isExtraSelectionDone();

    if (menuItemSelected != null && menuItemSelected.isCoffee() && !extraSelectionDone) {
      System.out.println(
          "You can choose an extra with it's code for your coffee product: "
              + checkSelectableExtras(selectedExtras)
              + " or say no(n)!");
    } else {
      orderService.closeOrder();
      backToMainMenu();
    }
  }

  private void addMenuItem(String menuCode) {
    MenuItem menuItemSelected = menuSelection.getMenuItemSelected();
    Stream<String> noOptions = Stream.of("n", "no");

    if (menuItemSelected.isCoffee() && noOptions.anyMatch(menuCode::equalsIgnoreCase)) {
      if (menuSelection.isExtraSelectionDone()) {
        System.out.println("Please give a valid number: > 0 as an input!");
      } else {
        System.out.println("Please type the quantity:");
      }
      menuSelection.setExtraSelectionDone(true);
    } else {
      addMenuItemWithExtraCheck(menuCode);
    }
  }

  private void addMenuItemWithExtraCheck(String menuCode) {
    boolean extraSelectionDone = menuSelection.isExtraSelectionDone();
    MenuItem menuItemSelected = menuSelection.getMenuItemSelected();
    List<MenuItem> selectedExtras = menuSelection.getSelectedExtras();

    if (!menuItemSelected.isCoffee()) {
      addNonExtraMenuItem(menuCode);
    } else if (extraSelectionDone) {
      if (MenuItem.checkIfExtraByCode(menuCode)) {
        System.out.println("Please give a valid number: > 0 as an input!");
      } else {
        addNonExtraMenuItem(menuCode);
      }
    } else if (MenuItem.checkIfExtraByCode(menuCode)) {
      addExtraMenuItem(MenuItem.getMenuItemByCode(menuCode));
    } else {
      System.out.println(
          "Please choose the coffee with a valid extra code: "
              + checkSelectableExtras(selectedExtras)
              + " or say no(n)!");
    }
  }

  private void addNonExtraMenuItem(String quantity) {
    if (!isValidNum(quantity)) {
      System.out.println("Please give a valid number: > 0 as an input!");
    } else {
      addNewOrderItemWithExtras(quantity);
    }
  }

  private void addExtraMenuItem(MenuItem extra) {
    List<MenuItem> selectedExtras = menuSelection.getSelectedExtras();
    boolean extraIsIn = selectedExtras.contains(extra);

    if (!extraIsIn) {
      selectedExtras.add(extra);
    }
    applySelectableCheck(extraIsIn ? extra : null);
  }

  private void addNewOrderItemWithExtras(String quantity) {
    MenuItem menuItemSelected = menuSelection.getMenuItemSelected();
    List<MenuItem> selectedExtras = menuSelection.getSelectedExtras();
    orderService.addNewOrderItem(menuItemSelected, quantity);

    selectedExtras.forEach(extraSelected -> orderService.addNewOrderItem(extraSelected, quantity));
    selectedExtras.clear();

    menuSelection.setMenuItemSelected(null);
    menuSelection.setExtraSelectionDone(false);
    System.out.println(
        "Please choose a product with it's code or submit(x), cancel(c) your order:");
  }

  private void applySelectableCheck(MenuItem extra) {
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

  private String checkSelectableExtras(List<MenuItem> selectedExtras) {
    return MenuItem.extras().stream()
        .filter(menuItemExtra -> !selectedExtras.contains(menuItemExtra))
        .map(MenuItem::getCode)
        .collect(Collectors.joining(", "));
  }

  private boolean isValidNum(String strNum) {
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
