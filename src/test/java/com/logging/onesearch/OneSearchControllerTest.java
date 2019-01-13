package com.logging.onesearch;

import com.logging.onesearch.controller.OneSearchController;
import com.logging.onesearch.service.S3Service;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by VijaySidhu on 1/13/2019.
 */
public class OneSearchControllerTest {


  @Mock
  Resource s3Resource;

  @Mock
  private S3Service s3Service;

  @Mock
  private MultipartFile multipartFile;

  private OneSearchController oneSearchController;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);

  }

  @Test
  public void listS3FilesTest() throws IOException {
    List<String> allFiles = Arrays.asList("test.json");
    Mockito.when(s3Service.getAllFiles()).thenReturn(allFiles);
    oneSearchController = new OneSearchController(s3Service);
    ResponseEntity<List<String>> s3Files = oneSearchController.listS3Files();
    Assert.assertEquals("test.json", s3Files.getBody().get(0));
    Mockito.verify(s3Service).getAllFiles();
  }

  @Test
  public void listS3FilesNullTest() throws IOException {
    Mockito.when(s3Service.getAllFiles()).thenReturn(null);
    oneSearchController = new OneSearchController(s3Service);
    ResponseEntity<List<String>> s3Files = oneSearchController.listS3Files();
    Assert.assertEquals(0, s3Files.getBody().size());
    Mockito.verify(s3Service).getAllFiles();
  }

  @Test
  public void downloadTest() throws IOException {
    String fileName = "test.json";
    ResponseEntity<Resource> resource = ResponseEntity
        .ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
        .body(s3Resource);

    Mockito.when(s3Service.downloadFile(fileName)).thenReturn(resource);
    oneSearchController = new OneSearchController(s3Service);
    ResponseEntity<Resource> responseEntity = oneSearchController.serveFile(fileName);
    List<String> val = responseEntity.getHeaders().get("Content-Disposition");
    Assert.assertEquals("attachment; filename=\"test.json\"", val.get(0));
    Mockito.verify(s3Service).downloadFile(fileName);
  }

  @Test
  public void deleteTest() {
    String fileName = "test.json";
    Mockito.doNothing().when(s3Service).deleteFile(fileName);
    oneSearchController = new OneSearchController(s3Service);
    oneSearchController.deleteFile(fileName);
    // verify service call
    Mockito.verify(s3Service).deleteFile(fileName);
  }

  @Test
  public void saveTest() throws IOException {
    String path = "s3://";
    String fileName = "test.json";
    Mockito.doNothing().when(s3Service).saveFile(multipartFile,path,fileName);
    oneSearchController = new OneSearchController(s3Service);
    oneSearchController.handleFileUpload(multipartFile,path,fileName);
    // verify service call
    Mockito.verify(s3Service).saveFile(multipartFile,path,fileName);
  }

  @Test
  public void searchTest() throws IOException {
    String pattern = "test.json";
    List<String> files = Arrays.asList("test.json");
    Mockito.when(s3Service.searchFile(pattern)).thenReturn(files);
    oneSearchController = new OneSearchController(s3Service);
    ResponseEntity<List<String>> s3Files = oneSearchController.searchS3Files(pattern);
    Assert.assertEquals(1, s3Files.getBody().size());
    Assert.assertEquals(pattern, s3Files.getBody().get(0));
    // verify service call
    Mockito.verify(s3Service).searchFile(pattern);
  }

}
