package org.swissre.assessment.service.menu;

import org.swissre.assessment.domain.MenuItem;

import java.util.Arrays;
import java.util.List;

public class MenuPrinter {

  public static void printMainMenu() {
    System.out.println("=========================");
    System.out.println("Coffee Corner");
    System.out.println("=========================");
    System.out.println("1. Create a new order");
    System.out.println("2. List all of the orders");
    System.out.println("3. Quit (Q)");
    System.out.println("=========================");
    System.out.println("Please choose from the menu: ");
  }

  public static void printCreateOrderMenu() {
    System.out.println("=================================");
    prettyPrintMenuItems(Arrays.asList(MenuItem.values()));
    System.out.println("=================================");
    System.out.println(
        "Please choose a product with the code or submit(x), cancel(c) your order:");
  }

  public static void prettyPrintMenuItems(List<MenuItem> menuItems) {
    String headerFormat = "%-17s %-9s %s";
    String rowFormat = "%-17s %-6s %.02f %s";

    System.out.printf((headerFormat) + "%n", "Product", "Code", "Price");
    System.out.println("---------------------------------");

    menuItems.stream()
            .map(menuItem -> MenuItemConverter.convertMenuItemToStr(rowFormat, menuItem))
            .forEach(System.out::println);
  }
}
