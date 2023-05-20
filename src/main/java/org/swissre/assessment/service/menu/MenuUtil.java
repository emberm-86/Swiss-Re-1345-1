package org.swissre.assessment.service.menu;

import static org.swissre.assessment.domain.MenuItem.checkIfExtraByCode;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.swissre.assessment.domain.MenuItem;
import org.swissre.assessment.domain.MenuSelection;
import org.swissre.assessment.domain.MenuState;
import org.swissre.assessment.domain.OrderItem;
import org.swissre.assessment.service.order.OrderService;

public class MenuUtil {

  public static void prettyPrintMenu(List<MenuItem> menuItems) {
    menuItems.stream().map(MenuUtil::convertMenuItemToStr).forEach(System.out::println);
  }

  public static String convertMenuItemToStr(MenuItem menuItem) {
    String format = "%-14s %-8s %.02f %s";
    return String.format(format, menuItem.getName(), " (" + menuItem.getCode() + ")",
        menuItem.getPrice(), "CHF");
  }

  public static void printMainMenu() {
    System.out.println("========================");
    System.out.println("Coffee Corner");
    System.out.println("========================");
    System.out.println("1. Create new order");
    System.out.println("2. List previous orders");
    System.out.println("3. Quit (Q)");
    System.out.println("========================");
    System.out.println("Please choose from the menu: ");
  }

  public static void printCreateOrderMenu() {
    System.out.println("================================");
    prettyPrintMenu(Arrays.asList(MenuItem.values()));
    System.out.println("================================");
    System.out.println(
        "Please choose a product with the code (second column) or submit your order(x), cancel(c): ");
  }

  public static boolean isValidNum(String strNum) {
    if (strNum == null) {
      return false;
    }
    try {
      return Integer.parseInt(strNum) > 0;
    } catch (NumberFormatException nfe) {
      return false;
    }
  }

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

  public static void launchCreateOrderMenu(String menuCode, MenuSelection menuSelection,
      OrderService orderService) {

    MenuItem menuItemSelected = menuSelection.getMenuItemSelected();
    List<MenuItem> selectedExtras = menuSelection.getSelectedExtras();
    boolean extraAlreadyChosen = menuSelection.isExtraAlreadyChosen();

    if (menuItemSelected == null) {

      if (Arrays.stream(MenuItem.codes())
          .noneMatch(menuItemCode -> menuItemCode.equals(menuCode))) {
        throw new IllegalArgumentException("You chose an invalid product code, please retry it!");
      }

      if (checkIfExtraByCode(menuCode)) {
        throw new IllegalArgumentException(
            "Please choose a coffee product before selecting any extras: sc, mc, lc!");
      }

      menuSelection.setMenuItemSelected(MenuItem.getMenuItemByCode(menuCode));

      if (MenuItem.isCoffee(menuCode)) {
        System.out.println(
            "You can choose an extra with codes to your coffee product: "
                + checkSelectableExtras(selectedExtras) + " or say no(n)!");
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
              String selectableExtras = checkSelectableExtras(selectedExtras);
              if (!selectableExtras.isEmpty()) {
                System.out.println(
                    "You can choose an other coffee extra with valid code: " + selectableExtras
                        + " or say no(n)!");
              } else {
                System.out.println(
                    "No selectable extras left, please type the quantity:");
                menuSelection.setExtraAlreadyChosen(true);
              }
            }
          } else {
            String selectableExtras = checkSelectableExtras(selectedExtras);
            if (!selectableExtras.isEmpty()) {
              System.out.println(
                  "This has already been chosen: " + extra.getCode() + ". Choose an other one " +
                      selectableExtras + " or say no(n)!");
            } else {
              System.out.println("No selectable extras left, please type the quantity:");
              menuSelection.setExtraAlreadyChosen(true);
            }
          }
        } else {
          if (menuItemSelected.isCoffee() && !(checkIfExtraByCode(menuCode)
              || extraAlreadyChosen)) {
            System.out.println(
                "Please choose a coffee with valid extra code: " +
                    checkSelectableExtras(selectedExtras) + " or say no(n)!");
          } else {
            if (!isValidNum(menuCode)) {
              System.out.println("Please give a valid number: > 0 as an input!");
            } else {
              orderService.addNewOrderItem(menuItemSelected, menuCode);

              selectedExtras.forEach(
                  extraSelected -> orderService.addNewOrderItem(extraSelected, menuCode));
              selectedExtras.clear();

              menuSelection.setMenuItemSelected(null);
              menuSelection.setExtraAlreadyChosen(false);

              System.out.println(
                  "Please choose a product with the code (second column) or submit your order(x) or cancel(c): ");
            }
          }
        }
      } else {
        menuSelection.setExtraAlreadyChosen(true);
        System.out.println("Please type the quantity: ");
      }
    }
  }

  public static String checkSelectableExtras(List<MenuItem> selectedExtras) {
    return MenuItem.extras().stream()
        .filter(menuItemExtra -> !selectedExtras.contains(menuItemExtra))
        .map(MenuItem::getCode)
        .collect(Collectors.joining(", "));
  }

  public static OrderItem convertToOrderItem(Entry<String, Integer> menuItemOcc) {
    MenuItem menuItem = MenuItem.getMenuItemByCode(menuItemOcc.getKey());
    return new OrderItem(menuItem, menuItemOcc.getValue());
  }
}
