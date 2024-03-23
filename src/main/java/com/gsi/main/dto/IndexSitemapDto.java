package com.gsi.main.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class IndexSitemapDto {
  private List<String> urls;
  private List<String> processedUrls;
  private List<String> exceptionUrls;
  private String sitemapUrl;
}
