package com.elice.aurasphere.contents.service;


import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.elice.aurasphere.contents.dto.S3UrlDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.util.Date;
import java.util.UUID;

@Service
public class S3Service {

    private final AmazonS3 amazonS3;


    public S3Service(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Transactional(readOnly = true)
    public S3UrlDTO getPutS3Url(String filename, String contentType) {
        // filename 설정하기(profile 경로 + 멤버ID + 랜덤 값)
        String key = "test/" + UUID.randomUUID() + "/" + filename;

        // url 유효기간 설정하기(1시간)
        Date expiration = getExpiration();

        // presigned url 생성하기
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                getPostGeneratePresignedUrlRequest(key, contentType, expiration);

        //url 생성
        URL url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);

        //url 담은 DTO 반환
        return S3UrlDTO.builder()
                .presignedUrl(url.toExternalForm())
                .key(key)
                .build();
    }

    @Transactional(readOnly = true)
    public S3UrlDTO getGetS3Url(String key) {
        // url 유효기간 설정하기(1시간)
        Date expiration = getExpiration();

        // presigned url 생성하기
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                getGetGeneratePresignedUrlRequest(key, expiration);

        URL url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);

        // return
        return S3UrlDTO.builder()
                .presignedUrl(url.toExternalForm())
                .key(key)
                .build();
    }

    /* post 용 URL 생성하는 메소드 */
    private GeneratePresignedUrlRequest getPostGeneratePresignedUrlRequest(
            String key, String contentType, Date expiration
    ) {
        GeneratePresignedUrlRequest generatePresignedUrlRequest
                = new GeneratePresignedUrlRequest(bucket, key)
                .withMethod(HttpMethod.PUT)
                .withKey(key)
                .withContentType(contentType)
                .withExpiration(expiration);
        return generatePresignedUrlRequest;
    }

    /* get 용 URL 생성하는 메소드 */
    private GeneratePresignedUrlRequest getGetGeneratePresignedUrlRequest(
            String key, Date expiration
    ) {
        return new GeneratePresignedUrlRequest(bucket, key)
                .withMethod(HttpMethod.GET)
                .withExpiration(expiration);
    }

    private static Date getExpiration() {
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * 60; // 1시간으로 설정하기
        expiration.setTime(expTimeMillis);
        return expiration;
    }
}
