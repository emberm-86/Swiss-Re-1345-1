package org.swissre.assessment.domain;

import static org.swissre.assessment.domain.Type.BEVERAGE;
import static org.swissre.assessment.domain.Type.EXTRA;
import static org.swissre.assessment.domain.Type.SNACK;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
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
  ORANGE_JUICE("oj", "Orange juice", BEVERAGE, new BigDecimal("3.95")),

  EXTRA_MILK("xem", "Extra milk", EXTRA, new BigDecimal("0.30")),
  FOAMED_MILK("xfm", "Foamed milk", EXTRA, new BigDecimal("0.50")),
  ROASTED_COFFEE("rcf", "Roasted coffee", EXTRA, new BigDecimal("0.90"));

  String code;
  String name;
  Type type;
  BigDecimal price;

  private static final Set<MenuItem> COFFEE_PRODUCTS =
      Stream.of(SMALL_COFFEE, MEDIUM_COFFEE, LARGE_COFFEE).collect(Collectors.toSet());

  public static String[] codes() {
    return Arrays.stream(values()).map(MenuItem::getCode).toArray(String[]::new);
  }

  public static MenuItem getMenuItemByCode(String code) {
    return Arrays.stream(values())
        .filter(menuItem -> menuItem.getCode().equalsIgnoreCase(code))
        .findFirst()
        .orElse(null);
  }

  public static boolean checkIfExtraByCode(String code) {
    return Arrays.stream(values())
        .anyMatch(menuIt -> menuIt.getCode().equalsIgnoreCase(code) && menuIt.getType() == EXTRA);
  }

  public static boolean isCoffee(String menuCode) {
    return getMenuItemByCode(menuCode).isCoffee();
  }

  public boolean isCoffee() {
    return COFFEE_PRODUCTS.contains(this);
  }

  public static List<MenuItem> extras() {
    return Arrays.stream(values())
        .filter(menuItem -> menuItem.getType() == EXTRA)
        .toList();
  }
}
