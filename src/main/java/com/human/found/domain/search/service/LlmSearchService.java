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

    public SearchConditionDTO interpret(SearchConditionDTO conditionDTO) {

        if (conditionDTO == null || conditionDTO.getKeyword() == null || conditionDTO.getKeyword().trim().isEmpty()) {
            return defaultCondition();
        }

        try {
            // 파이썬으로 보낼 JSON 바디에 사용자가 UI에서 선택한 조건들을 함께 패키징합니다.
            Map<String, Object> body = Map.of(
                "keyword", conditionDTO.getKeyword(),
                "boardType", conditionDTO.getBoardType() != null ? conditionDTO.getBoardType() : "all",
                "startDate", conditionDTO.getStartDate() != null ? conditionDTO.getStartDate() : "",
                "endDate", conditionDTO.getEndDate() != null ? conditionDTO.getEndDate() : ""
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            // Map<String, Object> 타입으로 엔티티 생성
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

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

            // 보장 로직: 파이썬이 혹시라도 "all"로 잘못 돌려주거나 누락했더라도, 
            // 사용자가 명시적으로 선택한 값이 있다면 스프링에서 최종 방어선으로 한 번 더 덮어써 줍니다.
            if (conditionDTO.getBoardType() != null 
                    && !"all".equals(conditionDTO.getBoardType())
                    && !conditionDTO.getBoardType().isBlank()) {
                result.setBoardType(conditionDTO.getBoardType());
            }
            if (conditionDTO.getStartDate() != null && !conditionDTO.getStartDate().isBlank()) {
                result.setStartDate(conditionDTO.getStartDate());
            }
            if (conditionDTO.getEndDate() != null && !conditionDTO.getEndDate().isBlank()) {
                result.setEndDate(conditionDTO.getEndDate());
            }

            // 기존 null 방어 코드들
            if (result.getBoardType() == null) result.setBoardType("all");
            if (result.getStatus() == null) result.setStatus("all");
            if (result.getCoreKeywords() == null) result.setCoreKeywords(new ArrayList<>());

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