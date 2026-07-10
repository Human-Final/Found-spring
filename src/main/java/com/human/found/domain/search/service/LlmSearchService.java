package com.human.found.domain.search.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.human.found.domain.search.dto.SearchConditionDTO;

@Service
public class LlmSearchService {

    @Value("${ai.search.interpret-url}")
    private String interpretUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public SearchConditionDTO interpret(String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return defaultCondition();
        }

        try {

            Map<String, String> body = Map.of(
                "keyword", keyword
            );

            // JSON으로 보내도록 명시
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<SearchConditionDTO> response = restTemplate.exchange(
                interpretUrl,
                HttpMethod.POST,
                entity,
                SearchConditionDTO.class
            );

            SearchConditionDTO result = response.getBody();

            if (result == null) {
                return defaultCondition();
            }

            if (result.getBoardType() == null) {
                result.setBoardType("all");
            }

            if (result.getStatus() == null) {
                result.setStatus("all");
            }

            if (result.getCoreKeywords() == null) {
                result.setCoreKeywords(new ArrayList<>());
            }

            return result;

        } catch (Exception e) {
            System.out.println("[LLM Search API 호출 실패] " + e.getMessage());
            return defaultCondition();
        }
    }

    private SearchConditionDTO defaultCondition() {
        SearchConditionDTO dto = new SearchConditionDTO();
        dto.setBoardType("all");
        dto.setStatus("all");
        dto.setCoreKeywords(new ArrayList<>());
        return dto;
    }
}