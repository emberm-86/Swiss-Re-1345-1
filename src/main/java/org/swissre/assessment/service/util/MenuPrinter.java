package org.swissre.assessment.service.util;

import org.swissre.assessment.domain.MenuItem;

import java.util.Arrays;
import java.util.List;

import static org.swissre.assessment.domain.Constants.FLT_FMT;

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
        "Please choose a product with it's code or submit(x), cancel(c) your order:");
  }

  public static void prettyPrintMenuItems(List<MenuItem> menuItems) {
    String headerFormat = "%-17s %-9s %s";
    String rowFormat = "%-17s %-6s " + FLT_FMT + " %s";

    System.out.printf((headerFormat) + "%n", "Product", "Code", "Price");
    System.out.println("---------------------------------");

    menuItems.stream()
        .map(menuItem -> MenuItemConverter.convertMenuItemToString(rowFormat, menuItem))
        .forEach(System.out::println);
  }
}
