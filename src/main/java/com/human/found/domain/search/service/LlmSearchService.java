package com.human.found.domain.search.service;

import org.springframework.stereotype.Service;

import com.human.found.domain.search.dto.SearchConditionDTO;

@Service
public class LlmSearchService {

    public SearchConditionDTO interpret(String query) {
        
        SearchConditionDTO conditionDTO = new SearchConditionDTO();

        // 기본값 초기화
        conditionDTO.setBoardType("all");
        conditionDTO.setStatus("all");

        if(query == null || query.isBlank()){
            return conditionDTO;
        }

        // 게시판 타입 추론
        if(query.contains("잃어") 
                || query.contains("분실") 
                || query.contains("못 본")
                || query.contains("찾고 있는") 
                || query.contains("없어") 
                || query.contains("안 보여")) {
            conditionDTO.setBoardType("found");
        }

        if(query.contains("주웠") 
                || query.contains("습득") 
                || query.contains("발견")) {
            conditionDTO.setBoardType("lost");
        }

        // 카데고리 추론
        if(query.contains("가방")
                || query.contains("백팩")
                || query.contains("숄더백")
                || query.contains("쇼퍼백")
                || query.contains("파우치") 
                || query.contains("에코백")
                || query.contains("쇼핑백")
                || query.contains("캐리어")
                || query.contains("배낭")){
            conditionDTO.setCategory("가방"); 
        } else if(query.contains("반지")
                || query.contains("목걸이")
                || query.contains("귀걸이")
                || query.contains("피어싱")
                || query.contains("시계")){
            conditionDTO.setCategory("귀금속");
        }else if(query.contains("도서")
                || query.contains("책")
                || query.contains("서적")
                || query.contains("소설")
                || query.contains("자기계발서")
                || query.contains("문제집")
                || query.contains("참고서")                
                || query.contains("만화")){
            conditionDTO.setCategory("도서");
        }else if(query.contains("모자")
                || query.contains("신발")
                || query.contains("점퍼")
                || query.contains("잠바")
                || query.contains("가디건")
                || query.contains("겉옷")
                || query.contains("패딩")
                || query.contains("코트")
                || query.contains("목도리")
                || query.contains("후리스")){
            conditionDTO.setCategory("의류");
        }else if(query.contains("자동차")
                || query.contains("번호판")
                || query.contains("네비")
                || query.contains("차 키")){
            conditionDTO.setCategory("자동차");
        }else if(query.contains("전자")
                || query.contains("노트북")
                || query.contains("맥북")
                || query.contains("컴퓨터")
                || query.contains("아이패드")
                || query.contains("이어폰")
                || query.contains("에어팟")
                || query.contains("충전기")
                || query.contains("헤드셋")
                || query.contains("헤드폰")
                || query.contains("카메라")
                || query.contains("스마트워치")
                || query.contains("USB")
                || query.contains("배터리")
                || query.contains("애플")
                || query.contains("핸드폰")
                || query.contains("휴대폰")
                || query.contains("갤럭시")
                || query.contains("아이폰")){
            conditionDTO.setCategory("전자기기");
        }else if(query.contains("지갑")){
            conditionDTO.setCategory("지갑");
        }else if(query.contains("카드")){
            conditionDTO.setCategory("카드");
        }

        // 색상 추론
        if (query.contains("검정") 
                || query.contains("검은") 
                || query.contains("챠콜") 
                || query.contains("그레이") 
                || query.contains("블랙")) {
            conditionDTO.setColor("검정");
        } else if (query.contains("흰색") 
                || query.contains("하얀") 
                || query.contains("화이트")
                || query.contains("아이보리")) {
            conditionDTO.setColor("흰색");
        } else if (query.contains("빨간") 
                || query.contains("빨강") 
                || query.contains("레드")) {
            conditionDTO.setColor("빨강");
        } else if (query.contains("파란") 
                || query.contains("파랑")
                || query.contains("푸른")
                || query.contains("하늘")                 
                || query.contains("블루")) {
            conditionDTO.setColor("파랑");
        }else if (query.contains("갈색") 
                || query.contains("베이지")){ 
            conditionDTO.setColor("갈색");
        }else if (query.contains("노랑") 
                || query.contains("노란")    
                || query.contains("옐로우")){ 
            conditionDTO.setColor("노랑");
        }else if (query.contains("초록") 
                || query.contains("그린")  
                || query.contains("연두")                  
                || query.contains("청록")){ 
            conditionDTO.setColor("초록");
        }else if (query.contains("보라") 
                || query.contains("바이올렛")    
                || query.contains("퍼플")){ 
            conditionDTO.setColor("보라");
        }else if (query.contains("주황") 
                || query.contains("오렌지")){    
            conditionDTO.setColor("주황");
        }else if (query.contains("핑크") 
                || query.contains("분홍")){    
            conditionDTO.setColor("분홍");
        }
        return conditionDTO;
    }

}
