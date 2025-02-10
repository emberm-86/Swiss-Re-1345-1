package org.swissre.assessment.domain;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class MenuSelection {
  private MenuState menuSelected = MenuState.MAIN_MENU;
  private MenuItem menuItemSelected;
  private List<MenuItem> selectedExtras = new ArrayList<>();
  private boolean extraSelectionDone;
}
