package org.swissre.assessment.service.menu;

import java.util.Arrays;
import java.util.List;
import org.swissre.assessment.domain.MenuItem;

public class MenuPrinter {

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
    prettyPrintMenuItems(Arrays.asList(MenuItem.values()));
    System.out.println("================================");
    System.out.println(
        "Please choose a product with the code(second column) or submit(x) or cancel(c) your order:");
  }

  public static void prettyPrintMenuItems(List<MenuItem> menuItems) {
    menuItems.stream().map(MenuItemConverter::convertMenuItemToStr).forEach(System.out::println);
  }
}
