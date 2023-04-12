package org.swissre.assessment.domain;

import static org.swissre.assessment.domain.Type.BEVERAGE;
import static org.swissre.assessment.domain.Type.EXTRA;
import static org.swissre.assessment.domain.Type.SNACK;

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

  SMALL_COFFEE("sc", "Small coffee", BEVERAGE, 2.5f),
  MEDIUM_COFFEE("mc", "Medium coffee", BEVERAGE, 3.0f),
  LARGE_COFFEE("lc", "Large coffee", BEVERAGE, 3.5f),
  BACON_ROLL("br", "Bacon roll", SNACK, 4.5f),
  ORANGE_JUICE("oj", "Orange Juice", BEVERAGE, 3.95f),

  EXTRA_MILK("xem", "Extra Milk", EXTRA, 0.3f),
  FOAMED_MILK("xfm", "Foamed Milk", EXTRA, 0.5f),
  ROASTED_COFFEE("rcf", "Roasted Coffee", EXTRA, 0.9f);

  String code;
  String name;
  Type type;
  float price;

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