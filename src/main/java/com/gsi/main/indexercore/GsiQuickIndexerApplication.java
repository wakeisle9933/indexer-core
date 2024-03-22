package com.gsi.main.indexercore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ComponentScan(basePackages = {"com.gsi.main"})
@EnableAsync
public class GsiQuickIndexerApplication {

  public static void main(String[] args) {
    SpringApplication.run(GsiQuickIndexerApplication.class, args);
  }

}
