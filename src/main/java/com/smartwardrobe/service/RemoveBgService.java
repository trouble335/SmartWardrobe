package com.smartwardrobe.service;

import com.smartwardrobe.config.OssConfig;
import com.smartwardrobe.exception.BusinessException;
import com.smartwardrobe.vo.FileUploadVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RemoveBgService {

    private final OssConfig ossConfig;
    private final FileService fileService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${removebg.api-key}")
    private String apiKey;

    @Value("${removebg.base-url:https://api.remove.bg/v1.0}")
    private String baseUrl;

    private static final com.aliyun.oss.OSS ossClient = null; // Will be injected

    public FileUploadVO removeBackground(String imageUrl) {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("Remove.bg API key not configured, returning original image");
            FileUploadVO vo = new FileUploadVO();
            vo.setUrl(imageUrl);
            vo.setOriginalUrl(imageUrl);
            return vo;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("X-Api-Key", apiKey);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("image_url", imageUrl);
            params.add("size", "auto");
            params.add("format", "png");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            ResponseEntity<byte[]> response = restTemplate.postForEntity(
                    baseUrl + "/removebg",
                    request,
                    byte[].class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error("Remove.bg调用失败: status={}", response.getStatusCode());
                throw new BusinessException(40004, "图片去背景失败");
            }

            // 上传处理后的图片到OSS
            String processedKey = "processed/" + UUID.randomUUID().toString() + "_nobg.png";
            com.aliyun.oss.OSS oss = new com.aliyun.oss.OSSClientBuilder().build(
                    ossConfig.getEndpoint(),
                    ossConfig.getAccessKeyId(),
                    ossConfig.getAccessKeySecret()
            );

            try {
                oss.putObject(ossConfig.getBucketName(), processedKey,
                        new java.io.ByteArrayInputStream(response.getBody()));
            } finally {
                oss.shutdown();
            }

            String processedUrl = ossConfig.getDomain() != null && !ossConfig.getDomain().isEmpty()
                    ? ossConfig.getDomain() + "/" + processedKey
                    : "https://" + ossConfig.getBucketName() + "." + ossConfig.getEndpoint() + "/" + processedKey;

            log.info("图片去背景成功: {}", processedUrl);

            FileUploadVO vo = new FileUploadVO();
            vo.setUrl(processedUrl);
            vo.setOriginalUrl(imageUrl);
            return vo;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("图片去背景异常", e);
            throw new BusinessException(40004, "图片去背景失败");
        }
    }
}
