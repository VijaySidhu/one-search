package com.logging.onesearch.controller;

/**
 * Created by VijaySidhu on 1/12/2019.
 */

import com.amazonaws.util.CollectionUtils;
import com.logging.onesearch.service.S3Service;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(path = "/api")
public class OneSearchController {

  private final S3Service s3Service;

  Logger logger = LoggerFactory.getLogger(OneSearchController.class);

  public OneSearchController(S3Service s3Service) {
    this.s3Service = s3Service;
  }

  @GetMapping(path = "/files")
  public ResponseEntity<List<String>> listS3Files() throws IOException {
    List<String> filesInS3Bucket = s3Service.getAllFiles();
    boolean isEmptyOrNull = CollectionUtils.isNullOrEmpty(filesInS3Bucket);
    ResponseEntity<List<String>> responseEntity =
        isEmptyOrNull ? ResponseEntity.ok().body(Collections.EMPTY_LIST)
            : ResponseEntity.ok().body(filesInS3Bucket);
    if (false == isEmptyOrNull) {
      logger.info("Number of Files in S3 bucket: {}", filesInS3Bucket.size());
    }
    return responseEntity;
  }

  @GetMapping("/download")
  public ResponseEntity<Resource> serveFile(@RequestParam String filename) {
    return s3Service.downloadFile(filename);
  }

  @DeleteMapping("/delete")
  public ResponseEntity<String> deleteFile(@RequestParam String filename) {
    logger.info("Deleting File: {} from S3", filename);
    s3Service.deleteFile(filename);
    return ResponseEntity
        .accepted()
        .body("File Deleted Successfully");
  }


  @PostMapping("/save")
  public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file,
      @RequestParam String path, @RequestParam String fileName) {

    try {
      s3Service.saveFile(file, path, fileName);
    } catch (IOException e) {
      // TODO Need to Create Global Exception Handler
      logger.error("Failed to upload file on S3");
      e.printStackTrace();
    }

    return ResponseEntity
        .accepted()
        .body("File Stored Successfully!");
  }

  @GetMapping(path = "/search")
  public ResponseEntity<List<String>> searchS3Files(@RequestParam String pattern)
      throws IOException {

    List<String> filesInS3Bucket = s3Service.searchFile(pattern);
    logger.info("Number of Files in S3 bucket: {}", filesInS3Bucket.size());

    return ResponseEntity
        .ok()
        .body(filesInS3Bucket);
  }


}
