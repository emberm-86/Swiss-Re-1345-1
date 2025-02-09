package org.swissre.assessment.service.util;

import java.util.Map.Entry;
import org.swissre.assessment.domain.MenuItem;
import org.swissre.assessment.domain.OrderItem;

import static org.swissre.assessment.domain.Constants.CURRENCY;

public class MenuItemConverter {

  public static OrderItem convertToOrderItem(Entry<String, Integer> menuItemCounter) {
    MenuItem menuItem = MenuItem.getMenuItemByCode(menuItemCounter.getKey());
    return new OrderItem(menuItem, menuItemCounter.getValue());
  }

  public static String convertMenuItemToString(String format, MenuItem menuIt) {
    return String.format(format, menuIt.getName(), menuIt.getCode(), menuIt.getPrice(), CURRENCY);
  }
}
