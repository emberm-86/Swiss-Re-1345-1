package org.swissre.assessment.domain;

import static org.swissre.assessment.domain.Type.BEVERAGE;
import static org.swissre.assessment.domain.Type.EXTRA;
import static org.swissre.assessment.domain.Type.SNACK;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@Getter
@FieldDefaults(makeFinal = true)
public enum MenuItem {

  SMALL_COFFEE("sc", "Small coffee", BEVERAGE, new BigDecimal("2.50")),
  MEDIUM_COFFEE("mc", "Medium coffee", BEVERAGE, new BigDecimal("3.00")),
  LARGE_COFFEE("lc", "Large coffee", BEVERAGE, new BigDecimal("3.50")),
  BACON_ROLL("br", "Bacon roll", SNACK, new BigDecimal("4.50")),
  ORANGE_JUICE("oj", "Orange Juice", BEVERAGE, new BigDecimal("3.95")),

  EXTRA_MILK("xem", "Extra Milk", EXTRA, new BigDecimal("0.3")),
  FOAMED_MILK("xfm", "Foamed Milk", EXTRA, new BigDecimal("0.5")),
  ROASTED_COFFEE("rcf", "Roasted Coffee", EXTRA, new BigDecimal("0.5"));

  String code;
  String name;
  Type type;
  BigDecimal price;

  @Override
  public String toString() {
    return name + "(" + code + ")" + String.format("%.02f", price);
  }

  public static String[] codes() {
    return Arrays.stream(values()).map(MenuItem::getCode).toArray(String[]::new);
  }

  public static MenuItem getMenuItemByCode(String code) {
    return Arrays.stream(values()).filter(menuItem -> menuItem.getCode().equalsIgnoreCase(code))
        .findFirst().orElse(null);
  }

  public static Boolean checkIfExtraByCode(String code) {
    return Arrays.stream(values()).anyMatch(menuItem -> menuItem.getCode().equalsIgnoreCase(code)
        && menuItem.getType() == EXTRA);
  }

  public static Boolean isCoffee(String menuCode) {
    return Stream.of(SMALL_COFFEE, MEDIUM_COFFEE, LARGE_COFFEE).collect(Collectors.toSet())
        .contains(getMenuItemByCode(menuCode));
  }

  public Boolean isCoffee() {
    return Stream.of(SMALL_COFFEE, MEDIUM_COFFEE, LARGE_COFFEE).collect(Collectors.toSet())
        .contains(this);
  }

  public static List<MenuItem> extras() {
    return Arrays.stream(values()).filter(menuItem -> menuItem.getType() == EXTRA)
        .collect(Collectors.toList());
  }
}