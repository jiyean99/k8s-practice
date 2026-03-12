//package com.beyond.order_system.common.storage;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//import software.amazon.awssdk.core.sync.RequestBody;
//import software.amazon.awssdk.services.s3.S3Client;
//import software.amazon.awssdk.services.s3.S3Uri;
//import software.amazon.awssdk.services.s3.S3Utilities;
//import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
//import software.amazon.awssdk.services.s3.model.PutObjectRequest;
//
//import java.io.IOException;
//import java.net.URI;
//import java.util.UUID;
//
//@Service
//public class S3ServiceImpl implements S3Service {
//
//    private final S3Client s3Client;
//
//    @Value("${aws.s3.bucket}")
//    private String bucket;
//
//    @Value("${aws.region}")
//    private String region;
//
//    public S3ServiceImpl(S3Client s3Client) {
//        this.s3Client = s3Client;
//    }
//
//    @Override
//    public S3UploadResult upload(MultipartFile file, String keyPrefix) {
//        String original = file.getOriginalFilename();
//        String ext = (original != null && original.contains(".")) ? original.substring(original.lastIndexOf('.')) : "";
//        String key = keyPrefix + "/" + UUID.randomUUID() + ext;
//
//        try {
//            PutObjectRequest req = PutObjectRequest.builder()
//                    .bucket(bucket)
//                    .key(key)
//                    .contentType(file.getContentType())
//                    .build();
//
//            s3Client.putObject(req, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
//        } catch (IOException e) {
//            throw new IllegalStateException("S3 업로드 실패", e);
//        }
//
//        String url = "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
//        return new S3UploadResult(key, url);
//    }
//
//    @Override
//    public void deleteByKey(String key) {
//        if (key == null || key.isBlank()) return;
//
//        DeleteObjectRequest req = DeleteObjectRequest.builder()
//                .bucket(bucket)
//                .key(key)
//                .build();
//
//        s3Client.deleteObject(req); // bucket+key로 삭제 [web:46]
//    }
//
//    @Override
//    public void deleteByUrl(String url) {
//        if (url == null || url.isBlank()) return;
//
//        S3Utilities utilities = s3Client.utilities();
//        S3Uri s3Uri = utilities.parseUri(URI.create(url)); // URL -> bucket/key 파싱 [web:58]
//
//        String key = String.valueOf(s3Uri.key());          // ex) products/10/uuid.png
//        String bucketToUse = s3Uri.bucket().orElse(bucket);
//
//        DeleteObjectRequest req = DeleteObjectRequest.builder()
//                .bucket(bucketToUse)
//                .key(key)
//                .build();
//
//        s3Client.deleteObject(req); // [web:46]
//    }
//}
