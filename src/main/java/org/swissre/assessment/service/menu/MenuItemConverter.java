package org.swissre.assessment.service.menu;

import java.util.Map.Entry;
import org.swissre.assessment.domain.MenuItem;
import org.swissre.assessment.domain.OrderItem;

import static org.swissre.assessment.domain.Constants.CURRENCY;

public class MenuItemConverter {

  public static OrderItem convertToOrderItem(Entry<String, Integer> menuItemOcc) {
    MenuItem menuItem = MenuItem.getMenuItemByCode(menuItemOcc.getKey());
    return new OrderItem(menuItem, menuItemOcc.getValue());
  }

  public static String convertMenuItemToStr(String format, MenuItem menuItem) {
    return String.format(format, menuItem.getName(), menuItem.getCode(),
            menuItem.getPrice(), CURRENCY);
  }
}
