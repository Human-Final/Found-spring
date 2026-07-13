package com.human.found.domain.search.service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.human.found.domain.search.dto.SearchConditionDTO;
import com.human.found.domain.search.mapper.SearchMapper;
import com.human.found.domain.search.vo.SearchResultVO;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService{
    
    private final SearchMapper searchMapper;
    private final LlmSearchService llmSearchService;
    

    // LIKE 기반 검색
    @Override
    public List<SearchResultVO> totalLikeSearch(SearchConditionDTO conditionDTO) {
       
        prepareLikeKeywords(conditionDTO);

        int totalCount = searchMapper.countTotalSearch(conditionDTO);
        conditionDTO.pageInfo(totalCount);

        return searchMapper.totalLikeSearch(conditionDTO);
    }


    // LIKE + LLM 병합 검색
    @Override
    public List<SearchResultVO> hybridSearch(
            SearchConditionDTO conditionDTO, 
            HttpServletRequest request){
        
        if (conditionDTO.getKeyword() == null 
                || conditionDTO.getKeyword().isBlank()) {

            conditionDTO.pageInfo(0);
            return List.of();
        }

        System.out.println("===== hybridSearch 실행 =====");
        System.out.println("keyword = " + conditionDTO.getKeyword());
        System.out.println("searchMode = " + conditionDTO.getSearchMode());
        
        //사용자 원문을 LIKE 검색용으로 준비
        prepareLikeKeywords(conditionDTO);

        // 자연어를 LLM 조건 DTO로 변환하여 별도 생성
        SearchConditionDTO llmCondition = llmSearchService.interpret(conditionDTO);

        // 사용자가 화면에서 선택한 필터 우선 적용
        applyUserFilters(conditionDTO, llmCondition, request);

        // LLM DTO에도 원문 LIKE 검색값을 그대로 전달
        llmCondition.setKeyword(conditionDTO.getKeyword());
        prepareLikeKeywords(llmCondition);

        List<SearchResultVO> likeList =searchMapper.candidateLikeSearch(llmCondition);
        List<SearchResultVO> llmList = List.of();

        if (hasLlmCondition(llmCondition)) {
            llmList = searchMapper.llmSearch(llmCondition);
        }else {
            System.out.println("LLM 실행 안 됨");
        }

        System.out.println("LLM boardType = " + llmCondition.getBoardType());
        System.out.println("LLM category = " + llmCondition.getCategory());
        System.out.println("LLM color = " + llmCondition.getColor());
        System.out.println("LLM place = " + llmCondition.getPlace());
        System.out.println("LLM coreKeywords = " + llmCondition.getCoreKeywords());
        System.out.println("LLM startDate = " + llmCondition.getStartDate());
        System.out.println("LLM endDate = " + llmCondition.getEndDate());

        // 사용자 화면에 검색 태그를 띄우기 위한 세터
        conditionDTO.setBoardType(llmCondition.getBoardType());
        conditionDTO.setCategory(llmCondition.getCategory());
        conditionDTO.setColor(llmCondition.getColor());
        conditionDTO.setPlace(llmCondition.getPlace());
        conditionDTO.setCoreKeywords(llmCondition.getCoreKeywords());

        // System.out.println("LLM 결과 수 = " + llmList.size());


        // like + LLM 병합, 중복 제거 + 점수 정렬 
        List<SearchResultVO> mergedList = 
                mergeSearchResults(likeList, llmList, llmCondition, conditionDTO);

        // System.out.println("병합 결과 수 = " + mergedList.size());

        // 병합 결과 기준 페이징
        conditionDTO.pageInfo(mergedList.size());

        int fromIndex = Math.min(conditionDTO.getOffset(), mergedList.size());
        int toIndex = Math.min(fromIndex + conditionDTO.getSize(), mergedList.size());
        
        return mergedList.subList(fromIndex, toIndex);
    }

    // 검색어 필터 : 사용자가 화면에서 직접 선택한 필터를 우선 반영
    // LLM이 추론한 게 있어도 사용자 값이 덮어 씀
    // 카테고리, 상태, 분실/습득 같은 태그들이 동작하게 되면 메서드 추가되어야 함
    private void applyUserFilters(
        SearchConditionDTO originalCondition, SearchConditionDTO llmCondition, HttpServletRequest request){

        String isUserSelectParam = request.getParameter("isUserSelect");
        boolean isUserSelect = "true".equals(isUserSelectParam);
        
        // 1. 분실물 / 습득물 여부 (boardType) 우선 반영
        // 사용자가 UI에서 'all', 'lost', 'found'를 명시적으로 선택했다면 무조건 덮어씀
        if (isUserSelect) {
            llmCondition.setBoardType(originalCondition.getBoardType());
            // System.out.println("[필터 강제 고정 활성화] 유저 선택 값으로 덮어씀: " + originalCondition.getBoardType());
        } else {
            // [RAG 자동 판단 모드] 사용자가 칩을 건드리지 않고 그냥 검색어만 친 경우라면,
            // 자바가 개입하지 않고 파이썬 RAG 모델이 분석해서 돌려준 값(found 또는 lost)을 100% 신뢰하고 보존합니다!
            // System.out.println("[RAG 자율 모드 활성화] 파이썬 판단 값을 그대로 사용합니다: " + llmCondition.getBoardType());
        }

        // 2. 날짜 범위 (startDate, endDate) 우선 반영
        // 사용자가 UI에서 시작 날짜를 명시적으로 골랐다면 덮어씀
        if (originalCondition.getStartDate() != null && !originalCondition.getStartDate().isBlank()) {
            llmCondition.setStartDate(originalCondition.getStartDate());
        }
        // 사용자가 UI에서 종료 날짜를 명시적으로 골랐다면 덮어씀
        if (originalCondition.getEndDate() != null && !originalCondition.getEndDate().isBlank()) {
            llmCondition.setEndDate(originalCondition.getEndDate());
        }
    }

    // LIKE 검색 결과와 LLM 검색 결과를 합쳐서 중복이면 'LIKE || LLM' 으로 표시하고 중복 제거
    // 병합 우선순위 : LIKE || LLM 우선 정렬 -> 날짜 -> 카테고리 -> 색상
    private List<SearchResultVO> mergeSearchResults(
            List<SearchResultVO> likeList,
            List<SearchResultVO> llmList,
            SearchConditionDTO llmCondition,
            SearchConditionDTO conditionDTO
    ){
        Map<String, SearchResultVO> resultMap = new LinkedHashMap<>();

        // LIKE 결과 먼저 등록
        for(SearchResultVO item : likeList){
            item.setMatchType("LIKE");
            resultMap.put(item.getSearchKey(), item);
        }
        
        // LLM 결과 병합
        for(SearchResultVO item : llmList){
            String key = item.getSearchKey();

            if(resultMap.containsKey(key)){
                // 동일한 게시글이 LIKE와 LLM 양쪽에서 검색된 경우
                SearchResultVO existing = resultMap.get(key);
                existing.setMatchType("LIKE || LLM");
            } else{
                // LLM 검색에서만 추가된 게시글
                item.setMatchType("LLM");
                resultMap.put(key, item);
            }
        }

        // 병합이 끝난 뒤 최종 관련도 점수 계산
        for (SearchResultVO item : resultMap.values()){
            int matchScore =
                calculateTotalPriority(
                        item,
                        conditionDTO,
                        llmCondition
                );

            item.setMatchScore(matchScore);        
        }

        // 1순위 : 관련도 점수 높은 순
        // 2순위 : 같은 점수면 최신 날짜순
        return resultMap.values()
                .stream()
                .sorted(
                    Comparator
                        .comparingInt(
                            SearchResultVO::getMatchScore
                        )
                        .reversed()
                        .thenComparing(
                            SearchResultVO::getEventDate,
                            Comparator.nullsLast(
                                Comparator.reverseOrder()
                            )
                        )
                )
                .toList();
    }


    // 점수 계산 메서드
    private int calculateTotalPriority(
            SearchResultVO item,
            SearchConditionDTO conditionDTO,
            SearchConditionDTO llmCondition) {

        int score = 0;

        // LLM 핵심 물건명 일치 여부
        List<String> coreKeywords =
                llmCondition.getCoreKeywords();

        boolean titleCoreMatch = false;
        boolean contentCoreMatch = false;

        if (coreKeywords != null
                && !coreKeywords.isEmpty()) {

            for (String keyword : coreKeywords) {

                if (containsText(
                        item.getTitle(),
                        keyword)) {

                    titleCoreMatch = true;
                }

                if (containsText(
                        item.getContent(),
                        keyword)) {

                    contentCoreMatch = true;
                }
            }
        }

        // LLM이 분석한 카테고리와 DB 카테고리 일치 여부
        String category =
                llmCondition.getCategory();

        boolean hasValidCategory =
                category != null
                && !category.isBlank()
                && !"all".equals(category)
                && !"기타".equals(category);

        boolean categoryMatch =
                hasValidCategory
                && containsText(
                        item.getCategory(),
                        category
                );

        // 코어키워드가 있을 때 최우선 점수 구간 설정
        if (coreKeywords != null
                && !coreKeywords.isEmpty()) {

            if (hasValidCategory) {

                // 제목의 핵심 물건명과 카테고리가 모두 일치
                if (titleCoreMatch
                        && categoryMatch) {

                    score += 10000;

                // 내용의 핵심 물건명과 카테고리가 모두 일치
                } else if (contentCoreMatch
                        && categoryMatch) {

                    score += 8000;

                // 핵심 물건명은 없지만 같은 물품 카테고리
                } else if (categoryMatch) {

                    score += 3000;

                // 핵심 물건명 문자열은 있지만 카테고리가 불일치
                } else if (titleCoreMatch
                        || contentCoreMatch) {

                    score += 1000;
                }

            } else {

                // 유효한 카테고리가 없는 경우에는 코어키워드만으로 판단
                if (titleCoreMatch) {

                    score += 10000;

                } else if (contentCoreMatch) {

                    score += 8000;
                }
            }

        // 코어키워드가 없으면 카테고리를 우선 반영
        } else if (categoryMatch) {

            score += 3000;
        }

        // 검색 경로 기본 점수
        // 코어키워드 점수보다 훨씬 작게 설정
        if ("LIKE || LLM".equals(
                item.getMatchType())) {

            score += 30;

        } else if ("LLM".equals(
                item.getMatchType())) {

            score += 20;

        } else if ("LIKE".equals(
                item.getMatchType())) {

            score += 10;
        }

        // 사용자 원문 전체 일치
        String originalKeyword =
                conditionDTO.getKeyword();

        if (originalKeyword != null
                && !originalKeyword.isBlank()) {

            if (containsText(
                    item.getTitle(),
                    originalKeyword)) {

                score += 300;

            } else if (containsText(
                    item.getContent(),
                    originalKeyword)) {

                score += 150;
            }
        }

        // 사용자 원문 분리 단어 점수
        List<String> likeKeywords =
                conditionDTO.getLikeKeywords();

        int likeKeywordScore = 0;

        if (likeKeywords != null) {
            for (String keyword : likeKeywords) {

                if (containsText(
                        item.getTitle(),
                        keyword)) {

                    likeKeywordScore += 20;

                } else if (containsText(
                        item.getContent(),
                        keyword)) {

                    likeKeywordScore += 10;

                } else if (containsText(
                        item.getCategory(),
                        keyword)) {

                    likeKeywordScore += 5;
                }
            }
        }

        // 특징어가 많아도 최대 60점까지만 반영
        score += Math.min(
                likeKeywordScore,
                60
        );

        // LLM이 정규화한 색상
        // 핵심 물건명과 카테고리가 일치하는 결과들 사이에서만 세부 순위 보정
        String color =
                llmCondition.getColor();

        if (color != null
                && !color.isBlank()
                && !"all".equals(color)) {

            if (containsText(
                    item.getColor(),
                    color)
                || containsText(
                    item.getTitle(),
                    color)
                || containsText(
                    item.getContent(),
                    color)) {

                score += 30;
            }
        }

        // LLM이 추출한 장소
        String place =
                llmCondition.getPlace();

        if (place != null
                && !place.isBlank()
                && !"all".equals(place)) {

            if (containsText(
                    item.getPlace(),
                    place)
                || containsText(
                    item.getTitle(),
                    place)
                || containsText(
                    item.getContent(),
                    place)) {

                score += 20;
            }
        }

        return score;
    }


    // LLM 실행 여부 : 카데고리, 색상, 장소, 분실/습득 날짜(범위)가 있으면 LLM 실행
    private boolean hasLlmCondition(SearchConditionDTO condition) {
        
        return (condition.getCategory() != null
                    && !condition.getCategory().isBlank()
                    && !"all".equals(condition.getCategory()))

                || (condition.getColor() != null
                    && !condition.getColor().isBlank()
                    && !"all".equals(condition.getColor()))

                || (condition.getPlace() != null
                    && !condition.getPlace().isBlank()
                    && !"all".equals(condition.getPlace()))

                || (condition.getStartDate() != null
                    && !condition.getStartDate().isBlank())

                || (condition.getEndDate() != null
                    && !condition.getEndDate().isBlank())
                    
                || (condition.getCoreKeywords() != null
                    && !condition.getCoreKeywords().isEmpty());


    }

    // LIKE 검색 시 공백 제거 후 비교
    private void prepareLikeKeywords(SearchConditionDTO conditionDTO){
        
        String keyword = conditionDTO.getKeyword();

        if (keyword == null || keyword.isBlank()) {
            conditionDTO.setKeyword("");
            conditionDTO.setKeywordNoSpace("");
            conditionDTO.setLikeKeywords(List.of());
            return;
        }

        String trimmedKeyword = keyword.trim();

        conditionDTO.setKeyword(trimmedKeyword);

        conditionDTO.setKeywordNoSpace(
                trimmedKeyword.replaceAll("\\s+", "")
        );

        conditionDTO.setLikeKeywords(
                Arrays.stream(trimmedKeyword.split("\\s+"))
                        .map(String::trim)
                        .filter(word -> !word.isBlank())
                        .distinct()
                        .toList()
        );
    }

    // 검색 결과가 특정 검색어를 포함하는지 확인
    private boolean containsText(
            String source,
            String keyword) {

        if (source == null
                || keyword == null
                || keyword.isBlank()) {

            return false;
        }

        String normalizedSource =
                source.replaceAll("\\s+", "");

        String normalizedKeyword =
                keyword.replaceAll("\\s+", "");

        return normalizedSource.contains(
                normalizedKeyword
        );
    }

}
