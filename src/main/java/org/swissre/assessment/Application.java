package org.swissre.assessment;

import java.util.Scanner;
import org.swissre.assessment.domain.MenuSelection;
import org.swissre.assessment.service.menu.MenuController;
import org.swissre.assessment.service.order.OrderServiceImpl;
import org.swissre.assessment.service.util.MenuPrinter;

public class Application {

  public static void main(String[] args) {
    MenuController menuController = new MenuController(new OrderServiceImpl(), new MenuSelection());
    MenuPrinter.printMainMenu();

    Scanner in = new Scanner(System.in);

    while (in.hasNext()) {
      String[] arguments = in.nextLine().split("\\s+");
      if (arguments.length == 0) {
        continue;
      }
      String menuCode = arguments[0];

      if (menuController.checkExit(menuCode)) break;
      if (menuController.preCheckMenuCode(menuCode)) continue;

      try {
        menuController.launchSelectedMenu(menuCode);
      } catch (IllegalArgumentException e) {
        System.out.println(e.getMessage());
      }
    }
    in.close();
    System.out.println("Good bye! :)");
  }
}
