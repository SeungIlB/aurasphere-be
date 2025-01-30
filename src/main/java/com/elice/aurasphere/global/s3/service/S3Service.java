package com.elice.aurasphere.global.s3.service;


import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.elice.aurasphere.global.exception.CustomException;
import com.elice.aurasphere.global.exception.ErrorCode;
import com.elice.aurasphere.global.s3.dto.S3UploadReqDTO;
import com.elice.aurasphere.global.s3.dto.S3UrlDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@Slf4j
@Service
public class S3Service {

    private final AmazonS3 amazonS3;


    public S3Service(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String uploadFile(MultipartFile file, String fileType) throws IOException {

        String directoryPath;

        // 파일 타입에 따라 경로 설정
        if ("image".equals(fileType)) {
            directoryPath = "images/";
        } else if ("video".equals(fileType)) {
            directoryPath = "videos/";
        } else if ("profile".equals(fileType)){
            directoryPath = "profiles/";
        } else {
            throw new CustomException(ErrorCode.FILE_TYPE_NOT_SUPPORTED);
        }

        String fileName = directoryPath + System.currentTimeMillis() + "_" + file.getOriginalFilename(); // 고유한 파일 이름 생성
        InputStream inputStream = file.getInputStream();

        // 메타데이터 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        // S3에 파일 업로드
        amazonS3.putObject(bucket, fileName, inputStream, metadata);

        // 업로드된 파일의 S3 URL 반환
        return amazonS3.getUrl(bucket, fileName).toString();
    }

    @Transactional(readOnly = true)
    public List<S3UrlDTO> getPutS3Url(List<S3UploadReqDTO> s3UploadReqDTOList) {

        List<S3UrlDTO> s3UrlDTO = new ArrayList<>();

        for(S3UploadReqDTO s3UploadReqDTO : s3UploadReqDTOList) {
            // filename 설정하기(profile 경로 + 멤버ID + 랜덤 값)
            String key = "test/" + UUID.randomUUID() + "/" + s3UploadReqDTO.getFileName();

            // url 유효기간 설정하기(1시간)
            Date expiration = getExpiration();

            // presigned url 생성하기
            GeneratePresignedUrlRequest generatePresignedUrlRequest =
                    getPostGeneratePresignedUrlRequest(key, s3UploadReqDTO.getContentType(), expiration);

            //url 생성
            URL url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);

            s3UrlDTO.add(S3UrlDTO.builder()
                    .presignedUrl(url.toExternalForm())
                    .key(key)
                    .build());
        }

        //url 담은 DTO 반환
        return s3UrlDTO;
    }

    @Transactional(readOnly = true)
    public String getGetS3Url(String key) {

        // presigned url 생성하기
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                getGetGeneratePresignedUrlRequest(key);

        URL url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);

        // return
        return url.toExternalForm();
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
    public GeneratePresignedUrlRequest getGetGeneratePresignedUrlRequest(
            String key
    ) {
        if( !amazonS3.doesObjectExist(bucket, key)) throw new CustomException(ErrorCode.IMAGE_NOT_FOUND);

        return new GeneratePresignedUrlRequest(bucket, key)
                .withMethod(HttpMethod.GET);
    }

    private static Date getExpiration() {
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * 60; // 1시간으로 설정하기
        expiration.setTime(expTimeMillis);
        return expiration;
    }
}