package org.swissre.assessment.service.menu;

import java.util.Map.Entry;
import org.swissre.assessment.domain.MenuItem;
import org.swissre.assessment.domain.OrderItem;

public class MenuItemConverter {

  public static OrderItem convertToOrderItem(Entry<String, Integer> menuItemOcc) {
    MenuItem menuItem = MenuItem.getMenuItemByCode(menuItemOcc.getKey());
    return new OrderItem(menuItem, menuItemOcc.getValue());
  }

  public static String convertMenuItemToStr(MenuItem menuItem) {
    String format = "%-14s %-8s %.02f %s";
    return String.format(format, menuItem.getName(), " (" + menuItem.getCode() + ")",
        menuItem.getPrice(), "CHF");
  }
}
