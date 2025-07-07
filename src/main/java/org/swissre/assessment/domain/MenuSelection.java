package org.swissre.assessment.domain;

import java.util.ArrayList;
import java.util.List;

public class MenuSelection {
  private MenuState menuSelected = MenuState.MAIN_MENU;
  private MenuItem menuItemSelected;
  private final List<MenuItem> selectedExtras = new ArrayList<>();
  private boolean extraSelectionDone;

  public MenuState getMenuSelected() {
    return menuSelected;
  }

  public void setMenuSelected(MenuState menuSelected) {
    this.menuSelected = menuSelected;
  }

  public MenuItem getMenuItemSelected() {
    return menuItemSelected;
  }

  public void setMenuItemSelected(MenuItem menuItemSelected) {
    this.menuItemSelected = menuItemSelected;
  }

  public List<MenuItem> getSelectedExtras() {
    return selectedExtras;
  }

  public boolean isExtraSelectionDone() {
    return extraSelectionDone;
  }

  public void setExtraSelectionDone(boolean extraSelectionDone) {
    this.extraSelectionDone = extraSelectionDone;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;

    MenuSelection that = (MenuSelection) o;
    return extraSelectionDone == that.extraSelectionDone && menuSelected == that.menuSelected && menuItemSelected == that.menuItemSelected && selectedExtras.equals(that.selectedExtras);
  }

  @Override
  public int hashCode() {
    int result = menuSelected.hashCode();
    result = 31 * result + menuItemSelected.hashCode();
    result = 31 * result + selectedExtras.hashCode();
    result = 31 * result + Boolean.hashCode(extraSelectionDone);
    return result;
  }
}
