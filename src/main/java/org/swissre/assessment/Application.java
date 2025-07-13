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
    boolean checkExit = false;

    while (!checkExit && in.hasNext()) {
      String[] arguments = in.nextLine().split("\\s+");
      String menuCode = arguments.length == 0 ? "" : arguments[0];
      checkExit = menuController.checkExit(menuCode);

      if (menuController.preCheckMenuCode(menuCode) || checkExit) continue;

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
