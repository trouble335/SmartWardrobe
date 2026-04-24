package com.smartwardrobe.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.smartwardrobe.config.OssConfig;
import com.smartwardrobe.exception.BusinessException;
import com.smartwardrobe.vo.FileUploadVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final OSS ossClient;
    private final OssConfig ossConfig;

    @Value("${app.image.max-size:10485760}")
    private Long maxSize;

    @Value("${app.image.allowed-types:image/jpeg,image/png,image/webp}")
    private String allowedTypes;

    public FileUploadVO upload(MultipartFile file, String type) {
        // 校验文件
        validateFile(file);

        // 生成文件路径
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;
        String objectKey = type + "/" + datePath + "/" + fileName;

        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putRequest = new PutObjectRequest(
                    ossConfig.getBucketName(), objectKey, inputStream);
            PutObjectResult result = ossClient.putObject(putRequest);

            String url = ossConfig.getDomain() != null && !ossConfig.getDomain().isEmpty()
                    ? ossConfig.getDomain() + "/" + objectKey
                    : "https://" + ossConfig.getBucketName() + "." + ossConfig.getEndpoint() + "/" + objectKey;

            log.info("文件上传成功: {}", url);

            FileUploadVO vo = new FileUploadVO();
            vo.setUrl(url);
            vo.setOriginalUrl(url);
            return vo;

        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(40005, "文件上传失败");
        }
    }

    public void delete(String fileUrl) {
        try {
            // 从URL中提取objectKey
            String objectKey = extractObjectKey(fileUrl);
            if (objectKey != null) {
                ossClient.deleteObject(ossConfig.getBucketName(), objectKey);
                log.info("文件删除成功: {}", objectKey);
            }
        } catch (Exception e) {
            log.error("文件删除失败: {}", fileUrl, e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(10001, "文件不能为空");
        }

        if (file.getSize() > maxSize) {
            throw new BusinessException(10001, "文件大小不能超过10MB");
        }

        String contentType = file.getContentType();
        List<String> allowedTypeList = Arrays.asList(allowedTypes.split(","));
        if (contentType == null || !allowedTypeList.contains(contentType)) {
            throw new BusinessException(10001, "不支持的文件类型");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    private String extractObjectKey(String url) {
        if (url == null) {
            return null;
        }
        try {
            // 移除域名部分
            if (url.startsWith("https://") || url.startsWith("http://")) {
                int pathStart = url.indexOf("/", url.indexOf("//") + 2);
                if (pathStart > 0) {
                    return url.substring(pathStart + 1);
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
