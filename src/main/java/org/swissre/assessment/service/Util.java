package org.swissre.assessment.service;

import java.util.Arrays;
import java.util.List;
import org.swissre.assessment.domain.MenuItem;

public class Util {

  public static void prettyPrintMenu(List<MenuItem> menuItems) {
    menuItems.stream().map(Util::printMenu).forEach(System.out::println);
  }

  public static String printMenu(MenuItem menuItem) {
    return String.format("%-14s %-8s %.02f %s", menuItem.getName(), " (" + menuItem.getCode() + ")",
        menuItem.getPrice(), " CHF");
  }

  public static void printMainMenu() {
    System.out.println("========================");
    System.out.println("Coffee Corner");
    System.out.println("========================");
    System.out.println("1. Create new order");
    System.out.println("2. List previous orders");
    System.out.println("3. Quit (Q)");
    System.out.println("========================");
    System.out.println("Please choose with a number: ");
  }

  public static void printCreateOrderMenu() {
    System.out.println("========================");
    prettyPrintMenu(Arrays.asList(MenuItem.values()));
    System.out.println("========================");
    System.out.println(
        "Please choose an other product with the code(second column) or submit your order(x), cancel(c): ");
  }

  public static boolean isNumeric(String strNum) {
    if (strNum == null) {
      return false;
    }
    try {
      Integer.parseInt(strNum);
    } catch (NumberFormatException nfe) {
      return false;
    }
    return true;
  }
}
