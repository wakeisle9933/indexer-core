package com.gsi.main.service;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.gson.GsonFactory;
import com.google.auth.oauth2.GoogleCredentials;
import com.gsi.main.dto.IndexCompletedDto;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class SitemapService {

  private static final String BASE_PATH = "src/main/resources/sitemap/";
  public List<String> extractUrls(String sitemapIndexUrl) {
    List<String> urls = new ArrayList<>();

    try {
      // 사이트맵 인덱스 파일에서 개별 사이트맵 URL 추출
      Document indexDoc = Jsoup.connect(sitemapIndexUrl).get();
      Elements sitemapElements = indexDoc.select("sitemapindex > sitemap > loc");

      for (Element sitemapElement : sitemapElements) {
        String sitemapUrl = sitemapElement.text();

        // 개별 사이트맵 파일에서 페이지 URL 추출
        Document sitemapDoc = Jsoup.connect(sitemapUrl).get();
        Elements urlElements = sitemapDoc.select("urlset > url > loc");

        for (Element urlElement : urlElements) {
          urls.add(urlElement.text());
        }
      }
    } catch (IOException e) {
      log.error("Error while extracting URLs from sitemap index: " + sitemapIndexUrl, e);
    }

    return urls;
  }

  public List<String> getIndexList(List<String> urls, List<String> processedUrls, List<String> exceptionUrls) {
    List<String> indexList = new ArrayList<>();
    for (String url : urls) {
      if (!processedUrls.contains(url) && !exceptionUrls.contains(url)) {
        indexList.add(url);
      }
    }
    return indexList;
  }


  public IndexCompletedDto indexUrls(List<String> urls, String sitemapUrl) throws IOException, InterruptedException {
    String scopes = "https://www.googleapis.com/auth/indexing";
    String endPoint = "https://indexing.googleapis.com/v3/urlNotifications:publish";
    GenericUrl genericUrl = new GenericUrl(endPoint);

    JsonFactory jsonFactory = new GsonFactory();
    InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("credential/credential.json");
    GoogleCredentials credentials = GoogleCredentials.fromStream(in).createScoped(Collections.singleton(scopes));
    HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory(request -> {
      request.setParser(new JsonObjectParser(jsonFactory));

      try {
        Map<String, List<String>> metadata = credentials.getRequestMetadata();
        HttpHeaders headers = new HttpHeaders();
        metadata.forEach((key, valueList) -> {
          if (!valueList.isEmpty()) {
            headers.set(key, valueList.get(0));
          }
        });
        request.setHeaders(headers);
      } catch (IOException e) {
        // 메타데이터를 가져오는 중 예외 처리
      }
    });

    LinkedList<String> succeedList = new LinkedList<>();
    int endIndex = Math.min(urls.size(), 2);
    List<String> dailyUrls = urls.subList(0, endIndex); // daily limit
    for (String url : dailyUrls) {
        String content = "{"
            + "\"url\": \"" + url + "\","
            + "\"type\": \"URL_UPDATED\","
            + "}";
        HttpRequest request = requestFactory.buildPostRequest(genericUrl,
            ByteArrayContent.fromString("application/json", content));
        HttpResponse response = request.execute();
        int statusCode = response.getStatusCode();
        if (statusCode == 200) {
          succeedList.add(url);
        }
        Thread.sleep(500);
    }

    String editPath = BASE_PATH + extractDomainName(sitemapUrl) + "_Processed";
    appendToFile(editPath, succeedList);
    return IndexCompletedDto.builder().success(true).count(succeedList.size()).build();
  }

  public void appendToFile(String filePath, LinkedList<String> successedList) {
    try {
      File file = new File(filePath);
      if (!file.exists()) {
        file.getParentFile().mkdirs();
        file.createNewFile();
      }

      FileWriter fw = new FileWriter(file, true); // true는 append mode를 의미합니다.
      BufferedWriter bw = new BufferedWriter(fw);
      PrintWriter out = new PrintWriter(bw);

      boolean isFirstLine = true;
      for (String line : successedList) {
        if (isFirstLine) {
          out.print("");
          out.println(line);
          isFirstLine = false;
        } else {
          out.println(line);
        }
      }

      out.close();
      bw.close();
      fw.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  public List<String> getProcessedUrls(String domain) {
    return readUrlsFromFile(getProcessedFilePath(domain));
  }

  public List<String> getExceptionUrls(String domain) {
    return readUrlsFromFile(getExceptionFilePath(domain));
  }

  private String getProcessedFilePath(String domain) {
    return BASE_PATH + extractDomainName(domain) + "_Processed";
  }

  private String getExceptionFilePath(String domain) {
    return BASE_PATH + extractDomainName(domain) + "_Exception";
  }

  private String extractDomainName(String url) {
    // HTTP, HTTPS 프로토콜과 서브 도메인을 제거하기 위해 URL을 소문자로 변환하고 처리합니다.
    url = url.toLowerCase();

    // 'http://', 'https://', 'www.'를 제거합니다.
    url = url.replaceAll("^(http://www\\.|http://|https://www\\.|https://|www\\.)", "");

    // URL을 '.'으로 나눕니다.
    String[] parts = url.split("\\.");
    if (parts.length >= 2) {
      // parts 배열에서 도메인 이름 바로 앞부분을 추출합니다.
      // 예: 'example.co.uk'에서 'example'을 반환합니다.
      String domain = parts[0];

      return domain;
    }

    // 예외 처리: 정상적인 도메인 형식이 아닌 경우, URL을 '_'로 치환하여 반환합니다.
    return url.replaceAll("[^a-zA-Z0-9]", "_");
  }

  private List<String> readUrlsFromFile(String filePath) {
    Path path = Paths.get(filePath);
    try {
      Path directory = path.getParent();
      if (directory != null && !Files.exists(directory)) {
        Files.createDirectories(directory);
      }

      if (!Files.exists(path)) {
        Files.createFile(path);
        log.info("Created file: " + filePath);
      }

      try (Stream<String> lines = Files.lines(path)) {
        return lines.filter(line -> !line.trim().isEmpty()).collect(Collectors.toList());
      }
    } catch (IOException e) {
      log.error("Failed to create or read file: " + filePath, e);
    }
    return new ArrayList<>();
  }

  public void openFileInNotepad(String sitemapUrl, String type) {
    String filePath = BASE_PATH + extractDomainName(sitemapUrl) + type;
    try {
      File file = new File(filePath);
      if (file.exists()) {
        Process process = Runtime.getRuntime().exec("notepad " + file.getAbsolutePath());
      } else {
        log.warn("File not found: " + filePath);
      }
    } catch (IOException e) {
      log.error("Failed to open file in Notepad: " + filePath, e);
    }
  }

}