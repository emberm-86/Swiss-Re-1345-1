package org.swissre.assessment.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class OrderItem {
  MenuItem menuItem;
  int quantity;
}
