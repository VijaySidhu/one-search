package com.logging.onesearch.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.WritableResource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by VijaySidhu on 1/12/2019.
 */

@Service
public class S3Service {

    private final ResourcePatternResolver resourcePatternResolver;

    private final ResourceLoader resourceLoader;

    private final AmazonS3 amazonS3;

    Logger logger = LoggerFactory.getLogger(S3Service.class);

    @Value("${aws.bucket.name}")
    private String bucket;

    public S3Service(ResourcePatternResolver resourcePatternResolver, ResourceLoader resourceLoader, AmazonS3 amazonS3) {
        this.resourcePatternResolver = resourcePatternResolver;
        this.resourceLoader = resourceLoader;
        this.amazonS3 = amazonS3;
    }

    public List<String> getAllFiles() throws IOException {
        String bucketPath = "s3://" + bucket + "/";
        Resource[] allFilesInFolder =  resourcePatternResolver.getResources(bucketPath+"**");
        List<Resource> resources = Arrays.asList(allFilesInFolder);
        List<String> filesInS3Bucket = new ArrayList<>();
        resources.forEach(f-> {
            filesInS3Bucket.add(f.getFilename());
        });
        Collections.sort(filesInS3Bucket);
        return filesInS3Bucket;
    }

    public void saveFile(MultipartFile file, String path, String fileName) throws IOException {

        String bucketPath = "s3://" + bucket + "/";
        Resource resource = this.resourceLoader.getResource(bucketPath+path+ "/"+ fileName);

        System.out.println("Storing File :"+bucketPath+path+ "/"+ fileName);

        WritableResource writableResource = (WritableResource) resource;

        try (OutputStream outputStream = writableResource.getOutputStream()) {
            byte[] bytes = IOUtils.toByteArray(file.getInputStream());

            logger.info("Storing file {} in S3", fileName);
            outputStream.write(bytes);
        }catch (Exception e){
            logger.error("Failed to upload file on S3");
            e.printStackTrace();
        }

    }

    public void deleteFile(String file) {
        amazonS3.deleteObject(bucket, file);
    }

    public List<String> searchFile(String pattern) throws IOException {
        String bucketPath = "s3://" + bucket + "/";
        Resource[] allFilesInFolder =  resourcePatternResolver.getResources(bucketPath + pattern);
        List<Resource> resources = Arrays.asList(allFilesInFolder);
        List<String> filesInS3Bucket = new ArrayList<>();

        resources.forEach(f-> {
            filesInS3Bucket.add(f.getFilename());
        });
        Collections.sort(filesInS3Bucket);

        return filesInS3Bucket;
    }

    public ResponseEntity<Resource> downloadFile(String filename){

        String bucketPath = "s3://" + bucket + "/";
        Resource s3Resource = resourceLoader.getResource(bucketPath + filename);

        String s3FileName = filename;//filename.substring(filename.lastIndexOf("/"));
        s3FileName = s3FileName.replace("/", "");

        logger.info("Downloading File: {} from S3", s3FileName);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""+s3FileName+"\"")
                .body(s3Resource);

    }

}
