package org.swissre.assessment.service.util;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swissre.assessment.domain.MenuItem;

import static org.swissre.assessment.domain.Constants.*;

public class MenuPrinter {

  private static final Logger LOGGER = LoggerFactory.getLogger(MenuPrinter.class);

  private MenuPrinter() {}

  public static void printMainMenu() {
    String menuSeparator = "=========================";
    LOGGER.info(menuSeparator);
    String title = String.format("%19s", "Coffee Corner");
    LOGGER.info(title);
    LOGGER.info(menuSeparator);
    LOGGER.info("1. Create a new order");
    LOGGER.info("2. List all of the orders");
    LOGGER.info("3. Quit (Q)");
    LOGGER.info(menuSeparator);
    LOGGER.info("Please choose from the menu: ");
  }

  public static void printCreateOrderMenu() {
    LOGGER.info("=================================");
    prettyPrintMenuItems(Arrays.asList(MenuItem.values()));
    LOGGER.info("=================================");
    LOGGER.info("Please choose a product with it's code or submit(x), cancel(c) your order:");
  }

  public static void prettyPrintMenuItems(List<MenuItem> menuItems) {
    String productShift = "%-17s";
    String headerFormat = productShift + " %-9s %s";
    String rowFormat = productShift + " %-6s " + FLT_FMT + " %s";

    String header = String.format(headerFormat, "Product", "Code", "Price");
    LOGGER.info(header);
    LOGGER.info("---------------------------------");

    menuItems.stream()
        .map(menuItem -> MenuItemConverter.convertMenuItemToString(rowFormat, menuItem))
        .forEach(LOGGER::info);
  }

  public static void printNumberValidationMsg(Logger logger) {
    logger.info(NUMBER_VALIDATION_MSG);
  }

  public static void printQuantityMsg(Logger logger) {
    logger.info(QUANTITY_MSG);
  }

  public static void printSelectExtraMsg(Logger logger, String leftExtras) {
    logger.info(SELECT_EXTRA_MSG, leftExtras, OR_SAY_NO_MSG);
  }
}
