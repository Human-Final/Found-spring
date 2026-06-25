package com.human.found.domain.search.service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.human.found.domain.search.dto.SearchConditionDTO;
import com.human.found.domain.search.mapper.SearchMapper;
import com.human.found.domain.search.vo.SearchResultVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService{
    
    private final SearchMapper searchMapper;
    private final LlmSearchService llmSearchService;
    
    // LIKE 기반 검색
    @Override
    public List<SearchResultVO> totalLikeSearch(SearchConditionDTO conditionDTO) {
       
        int totalCount = searchMapper.countTotalSearch(conditionDTO);
        conditionDTO.pageInfo(totalCount);

        return searchMapper.totalLikeSearch(conditionDTO);
    }

    // LIKE + LLM 병합 검색
    @Override
    public List<SearchResultVO> hybridSearch(SearchConditionDTO conditionDTO){
        
        // LIKE 기반 검색
        List<SearchResultVO> likeList = searchMapper.totalLikeSearch(conditionDTO);
        
        // 자연어를 LLM 조건 DTO로 변환
        SearchConditionDTO llmCondition = 
            llmSearchService.interpret(conditionDTO.getKeyword());
        
        // 사용자가 화면에서 선택한 필터 반영
        applyUserFilters(conditionDTO, llmCondition);

        // LLM 기반 검색
        List<SearchResultVO> llmList = List.of();

        if (hasLlmCondition(llmCondition)) {
            llmList = searchMapper.llmSearch(llmCondition);
        }

        // like + LLM 
        List<SearchResultVO> mergedList = mergeSearchResults(likeList, llmList);

        // 병합 결과 기준 페이징
        conditionDTO.pageInfo(mergedList.size());

        int fromIndex = Math.min(conditionDTO.getOffset(), mergedList.size());
        int toIndex = Math.min(fromIndex + conditionDTO.getSize(), mergedList.size());
        
        return mergedList.subList(fromIndex, toIndex);
    }

    private void applyUserFilters(
            SearchConditionDTO likeCondition, SearchConditionDTO llmCondition){

        if(likeCondition.getBoardType() != null
                && !likeCondition.getBoardType().isBlank()
                && !"all".equals(likeCondition.getBoardType())){
            llmCondition.setBoardType(likeCondition.getBoardType());
        }

        if(likeCondition.getStatus() != null
                && !likeCondition.getStatus().isBlank()
                && !"all".equals(likeCondition.getStatus())){
            llmCondition.setStatus(likeCondition.getStatus());
        }
        
        if(likeCondition.getCategory() != null
                && !likeCondition.getCategory().isBlank()
                && !"all".equals(likeCondition.getCategory())){
            llmCondition.setCategory(likeCondition.getCategory());
        }

        if(likeCondition.getColor() != null
                && !likeCondition.getColor().isBlank()
                && !"all".equals(likeCondition.getColor())){
            llmCondition.setColor(likeCondition.getColor());
        }

        if(likeCondition.getPlace() != null
                && !likeCondition.getPlace().isBlank()
                && !"all".equals(likeCondition.getPlace())){
            llmCondition.setPlace(likeCondition.getPlace());
        }
        
        if(likeCondition.getEventDate() != null
                && !likeCondition.getEventDate().isBlank()
                && !"all".equals(likeCondition.getEventDate())){
            llmCondition.setEventDate(likeCondition.getEventDate());
        }
        
        if(likeCondition.getStartDate() != null
                && !likeCondition.getStartDate().isBlank()
                && !"all".equals(likeCondition.getStartDate())){
            llmCondition.setStartDate(likeCondition.getStartDate());
        }
        
        if(likeCondition.getEndDate() != null
                && !likeCondition.getEndDate().isBlank()
                && !"all".equals(likeCondition.getEndDate())){
            llmCondition.setEndDate(likeCondition.getEndDate());
        }        
    }

    private List<SearchResultVO> mergeSearchResults(
            List<SearchResultVO> likeList,
            List<SearchResultVO> llmList
    ){
        Map<String, SearchResultVO> resultMap = new LinkedHashMap<>();

        for(SearchResultVO item : likeList){
            item.setMatchType("LIKE");
            item.setMatchScore(1);
            resultMap.put(item.getSearchKey(), item);
        }
    
        for(SearchResultVO item : llmList){
            String key = item.getSearchKey();

            if(resultMap.containsKey(key)){
                SearchResultVO existing = resultMap.get(key);
                existing.setMatchType("LIKE || LLM");
                existing.setMatchScore(2);
            } else{
                item.setMatchType("LLM");
                item.setMatchScore(1);
                resultMap.put(key, item);
            }
        }

        return resultMap.values().stream()
                    .sorted(
                        Comparator
                            .comparing(
                                SearchResultVO::getMatchScore,
                                Comparator.nullsLast(Comparator.reverseOrder())
                            )
                            .thenComparing(
                                SearchResultVO::getEventDate,
                                Comparator.nullsLast((Comparator.reverseOrder()))
                            )
                            
                    )
                    .toList();
    }
    
    private boolean hasLlmCondition(SearchConditionDTO condition) {
        
        int conditionCount = 0;

        if (condition.getBoardType() != null
                && !condition.getBoardType().isBlank()
                && !"all".equals(condition.getBoardType())) {
            conditionCount++;
        }

        if (condition.getCategory() != null
                && !condition.getCategory().isBlank()
                && !"all".equals(condition.getCategory())) {
            conditionCount++;
        }

        if (condition.getColor() != null
                && !condition.getColor().isBlank()
                && !"all".equals(condition.getColor())) {
            conditionCount++;
        }

        if (condition.getPlace() != null
                && !condition.getPlace().isBlank()
                && !"all".equals(condition.getPlace())) {
            conditionCount++;
        }

        if (condition.getEventDate() != null
                && !condition.getEventDate().isBlank()) {
            conditionCount++;
        }

        if (condition.getStartDate() != null
                && !condition.getStartDate().isBlank()) {
            conditionCount++;
        }

        if (condition.getEndDate() != null
                && !condition.getEndDate().isBlank()) {
            conditionCount++;
        }
    return conditionCount >= 2;
}

}
