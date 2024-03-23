package com.gsi.main.controller;

import com.gsi.main.dto.IndexCompletedDto;
import com.gsi.main.dto.IndexSitemapDto;
import com.gsi.main.service.SitemapService;
import java.io.IOException;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sitemaps")
@RequiredArgsConstructor
public class SitemapController {

  private final SitemapService sitemapService;

  @PostMapping
  public Map<String, List<String>> addSitemap(@RequestBody SitemapRequest request) {
    IndexSitemapDto dto = IndexSitemapDto.builder()
        .urls(sitemapService.extractUrls(request.getUrl()))
        .processedUrls(sitemapService.getProcessedUrls(request.getUrl()))
        .exceptionUrls(sitemapService.getExceptionUrls(request.getUrl()))
        .build();
    List<String> urls = sitemapService.getIndexList(dto.getUrls(), dto.getProcessedUrls(), dto.getExceptionUrls());
    List<String> processedUrls = dto.getProcessedUrls();
    List<String> exceptionUrls = dto.getExceptionUrls();
    return Map.of("sitemapUrls", urls, "processedUrls", processedUrls, "exceptionUrls", exceptionUrls);
  }

  @PostMapping("/indexing")
  public IndexCompletedDto updateIndex(@RequestBody IndexSitemapDto indexSitemap)
      throws IOException, InterruptedException {
    return sitemapService.indexUrls(sitemapService.getIndexList(indexSitemap.getUrls()
                            , indexSitemap.getProcessedUrls()
                            , indexSitemap.getExceptionUrls()), indexSitemap.getSitemapUrl());
  }

  @GetMapping("/files/processed")
  public void openProcessedFile(@RequestParam String sitemapUrl) {
    sitemapService.openFileInNotepad(sitemapUrl, "_Processed");
  }

  @GetMapping("/files/exception")
  public void openExceptionFile(@RequestParam String sitemapUrl) {
    sitemapService.openFileInNotepad(sitemapUrl, "_Exception");
  }

  @Getter
  @Setter
  static class SitemapRequest {
    private String url;
  }

}