package com.tabnote.server.tabnoteserverboot.services;

import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RagService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);

    private final RestTemplate restTemplate;
    private final DiscoveryClient discoveryClient;
    private static final String RAG_SERVICE_NAME = "TabNoteRAGEnhanced";

    public RagService(RestTemplate restTemplate, DiscoveryClient discoveryClient) {
        this.restTemplate = restTemplate;
        this.discoveryClient = discoveryClient;
    }

    private String getRagBaseUrl() {
        try {
            // 从 Nacos 服务发现中获取 TabNoteRAGEnhanced 服务的实例
            java.util.List<ServiceInstance> instances = discoveryClient.getInstances(RAG_SERVICE_NAME);
            if (instances != null && !instances.isEmpty()) {
                ServiceInstance instance = instances.get(0);
                String baseUrl = "http://" + instance.getHost() + ":" + instance.getPort();
                log.info("RAG service discovered at: {}", baseUrl);
                return baseUrl;
            } else {
                log.warn("No RAG service instance found, using default URL");
                // 如果没有发现服务，使用默认地址作为 fallback
                return "http://127.0.0.1:7859";
            }
        } catch (Exception e) {
            log.error("Error discovering RAG service: {}", e.getMessage());
            // 发生错误时，使用默认地址作为 fallback
            return "http://127.0.0.1:7859";
        }
    }

    public boolean insertContent(String subject, String text) {
        try {
            String url = getRagBaseUrl() + "/basic/insert";
            
            JSONObject requestBody = new JSONObject();
            requestBody.put("subject", subject);
            requestBody.put("text", text);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("RAG insert successful for subject: {}", subject);
                return true;
            } else {
                log.error("RAG insert failed for subject: {}, status: {}", subject, response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            log.error("RAG insert error for subject: {}, error: {}", subject, e.getMessage());
            return false;
        }
    }

    public JSONObject searchContent(String subject, String text, int limit, int minDistance) {
        try {
            String url = getRagBaseUrl() + "/basic/select";
            
            JSONObject requestBody = new JSONObject();
            requestBody.put("subject", subject);
            requestBody.put("text", text);
            requestBody.put("limit", limit);
            requestBody.put("minDistance", minDistance);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("RAG search successful for subject: {}", subject);
                return JSONObject.parseObject(response.getBody());
            } else {
                log.error("RAG search failed for subject: {}, status: {}", subject, response.getStatusCode());
                JSONObject errorResult = new JSONObject();
                errorResult.put("success", false);
                errorResult.put("message", "Search failed");
                return errorResult;
            }
        } catch (Exception e) {
            log.error("RAG search error for subject: {}, error: {}", subject, e.getMessage());
            JSONObject errorResult = new JSONObject();
            errorResult.put("success", false);
            errorResult.put("message", e.getMessage());
            return errorResult;
        }
    }

    public boolean insertContentWithParagraphs(String subject, String text) {
        try {
            // 按换行符分割内容
            String[] paragraphs = text.split("\\n");
            
            // 过滤掉空段落和只包含空白字符的段落
            java.util.List<String> filteredParagraphs = new java.util.ArrayList<>();
            for (String paragraph : paragraphs) {
                String trimmed = paragraph.trim();
                if (!trimmed.isEmpty()) {
                    filteredParagraphs.add(trimmed);
                }
            }
            
            // 如果没有有效段落，直接返回成功
            if (filteredParagraphs.isEmpty()) {
                log.info("No valid paragraphs to insert for subject: {}", subject);
                return true;
            }
            
            String url = getRagBaseUrl() + "/basic/insert_array";
            
            JSONObject requestBody = new JSONObject();
            requestBody.put("subject", subject);
            requestBody.put("textArray", filteredParagraphs);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("RAG insert_array successful for subject: {}, inserted {} paragraphs", subject, filteredParagraphs.size());
                return true;
            } else {
                log.error("RAG insert_array failed for subject: {}, status: {}", subject, response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            log.error("RAG insert_array error for subject: {}, error: {}", subject, e.getMessage());
            return false;
        }
    }
}