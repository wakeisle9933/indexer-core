package com.gsi.main.controller;

import com.gsi.main.service.SitemapService;
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
    List<String> urls = sitemapService.extractUrls(request.getUrl());
    List<String> processedUrls = sitemapService.getProcessedUrls(request.getUrl());
    List<String> exceptionUrls = sitemapService.getExceptionUrls(request.getUrl());
    return Map.of("sitemapUrls", urls, "processedUrls", processedUrls, "exceptionUrls", exceptionUrls);
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