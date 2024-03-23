package com.gsi.main.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class IndexCompletedDto {
  private boolean success;
  private int count;
}
