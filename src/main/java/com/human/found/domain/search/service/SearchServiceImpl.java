package com.human.found.domain.search.service;

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
       
        setKeywordNoSpace(conditionDTO);

        int totalCount = searchMapper.countTotalSearch(conditionDTO);
        conditionDTO.pageInfo(totalCount);

        return searchMapper.totalLikeSearch(conditionDTO);
    }


    // LIKE + LLM 병합 검색
    @Override
    public List<SearchResultVO> hybridSearch(SearchConditionDTO conditionDTO, HttpServletRequest request){
        
        if (conditionDTO.getKeyword() == null || conditionDTO.getKeyword().isBlank()) {
            conditionDTO.pageInfo(0);
            return List.of();
        }

        System.out.println("===== hybridSearch 실행 =====");
        System.out.println("keyword = " + conditionDTO.getKeyword());
        System.out.println("searchMode = " + conditionDTO.getSearchMode());

        setKeywordNoSpace(conditionDTO);

        // 자연어를 LLM 조건 DTO로 변환
        SearchConditionDTO llmCondition = llmSearchService.interpret(conditionDTO);

        // 사용자가 화면에서 선택한 필터 반영
        applyUserFilters(conditionDTO, llmCondition, request);
        
        // LIKE 기반 검색
        List<SearchResultVO> likeList = searchMapper.candidateLikeSearch(llmCondition);

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

        // LLM 기반 검색
        List<SearchResultVO> llmList = List.of();

        if (hasLlmCondition(llmCondition)) {
            llmList = searchMapper.llmSearch(llmCondition);
            System.out.println("LLM 검색 실행됨");
        }else {
            System.out.println("LLM 실행 안 됨");
        }

        System.out.println("LLM 결과 수 = " + llmList.size());


        // like + LLM 
        List<SearchResultVO> mergedList = mergeSearchResults(likeList, llmList, llmCondition);

        System.out.println("병합 결과 수 = " + mergedList.size());

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
            System.out.println("[필터 강제 고정 활성화] 유저 선택 값으로 덮어씀: " + originalCondition.getBoardType());
        } else {
            // 🤖 [RAG 자동 판단 모드] 사용자가 칩을 건드리지 않고 그냥 검색어만 친 경우라면,
            // 자바가 개입하지 않고 파이썬 RAG 모델이 분석해서 돌려준 값(found 또는 lost)을 100% 신뢰하고 보존합니다!
            System.out.println("[RAG 자율 모드 활성화] 파이썬 판단 값을 그대로 사용합니다: " + llmCondition.getBoardType());
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
            SearchConditionDTO llmCondition
    ){
        Map<String, SearchResultVO> resultMap = new LinkedHashMap<>();

        for(SearchResultVO item : likeList){
            item.setMatchType("LIKE");
            resultMap.put(item.getSearchKey(), item);
        }
    
        for(SearchResultVO item : llmList){
            String key = item.getSearchKey();

            if(resultMap.containsKey(key)){
                SearchResultVO existing = resultMap.get(key);
                existing.setMatchType("LIKE || LLM");
            } else{
                item.setMatchType("LLM");
                resultMap.put(key, item);
            }
        }

        // 병합이 끝난 뒤 최종 관련도 점수 계산
        for (SearchResultVO item : resultMap.values()){
            item.setMatchScore(calculateTotalPriority(item, llmCondition));
        }

        return resultMap.values().stream()
                    .sorted(
                        Comparator
                            // 관련도 점수 높은 순
                            .comparing(
                                SearchResultVO::getMatchScore,
                                Comparator.nullsLast(Comparator.reverseOrder())
                            )
                            // 같은 점수면 최신 날짜순
                            .thenComparing(
                                SearchResultVO::getEventDate,
                                Comparator.nullsLast((Comparator.reverseOrder()))
                            )                            
                    )
                    .toList();
    }


    // 점수 계산 메서드
    private int calculateTotalPriority(
            SearchResultVO item, SearchConditionDTO conditionDTO){
        
        int score = 0;

        // LIKE와 LLM 둘 다 걸린 결과 최우선
        if("LIKE || LLM".equals(item.getMatchType())){
            score += 100;
        }else if("LIKE".equals(item.getMatchType()) || "LLM".equals(item.getMatchType())){
            score += 50;
        }

        String category = conditionDTO.getCategory();
        String place = conditionDTO.getPlace();
        String color = conditionDTO.getColor();        
        List<String> coreKeywords = conditionDTO.getCoreKeywords();

        if (coreKeywords != null && !coreKeywords.isEmpty()) {
            for (String kw : coreKeywords) {
                if (containsText(item.getTitle(), kw)) {
                    score += 200;
                } else if (containsText(item.getContent(), kw)) {
                    score += 120;
                } else if (containsText(item.getCategory(), kw)) {
                    score += 60;
                }
            }
        }

        // 카테고리 일치 : 100점
        if(category != null && !category.isBlank() && !"all".equals(category)){
            if(containsText(item.getCategory(), category)
                    || containsText(item.getTitle(), category)
                    || containsText(item.getContent(), category)){
                score += 100;
            }
        }

        // 장소 일치 : 30점
        if (place != null && !place.isBlank() && !"all".equals(place)) {
            if (containsText(item.getPlace(), place)
                    || containsText(item.getTitle(), place)
                    || containsText(item.getContent(), place)) {
                score += 30;
            }
        }

        // 색상 일치 : 20점
        if (color != null && !color.isBlank() && !"all".equals(color)) {
            if (containsText(item.getColor(), color)
                    || containsText(item.getTitle(), color)
                    || containsText(item.getContent(), color)) {
                score += 20;
            }
        }
        
        return score;
    }


    // 검색 결과가 LLM이 뽑은 조건을 실제로 포함하는지 확인하는 보조 메서드
    private boolean containsText(String source, String keyword){
        if(source == null || keyword == null){
            return false;
        }

        // 공백 없애기 : 관련도 점수 계산할 때 띄어쓰기로 검색결과가 달라지는 것을 방지
        String normalizedSource = source.replaceAll("\\s+", "");
        String normalizedKeyword = keyword.replaceAll("\\s+", "");

        return normalizedSource.contains(normalizedKeyword);
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
    private void setKeywordNoSpace(SearchConditionDTO conditionDTO){
        if (conditionDTO.getKeyword() != null){
            conditionDTO.setKeywordNoSpace(
                conditionDTO.getKeyword().replaceAll("\\s+", ""));
        }
    }

}
