package com.human.found.infrastructure.policeAPI.foundAPI.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.human.found.domain.found.vo.FoundVO;
import com.human.found.infrastructure.policeAPI.foundAPI.mapper.FoundPoliceMapper;
import com.human.found.infrastructure.policeAPI.foundAPI.mapper.FoundPortalMapper;
import com.human.found.infrastructure.policeAPI.foundAPI.vo.FoundPortalApiItemVO;

import lombok.RequiredArgsConstructor;



@Service
@RequiredArgsConstructor
public class FoundPortalTxService {

    private final FoundPortalMapper foundPortalMapper;
    private final FoundPoliceMapper foundPoliceMapper;
    private static final int BATCH_SIZE = 500;

    @Transactional
    public int upsertFoundPortalItems(
            List<FoundPortalApiItemVO> items, 
            LocalDate today, 
            LocalDate sixMonthsAgo) {

        // foundPortalMapper.deleteAllFoundPortal();

        List<FoundVO> foundList = convertItems(items, today, sixMonthsAgo);
        
        if(foundList.isEmpty()){
            return 0;
        }

        int saveCount = 0;

        for (int i = 0; i < foundList.size(); i += BATCH_SIZE) {
            
            int end = Math.min(i + BATCH_SIZE, foundList.size());

            List<FoundVO> batchList = foundList.subList(i, end);

            foundPortalMapper.upsertFoundPortal(batchList);
            saveCount += batchList.size();
        }

        // upsert가 정상 완료된 뒤 오래된 데이터 삭제
        foundPortalMapper.markOldFoundPortalDeleted(sixMonthsAgo.atStartOfDay());

        return saveCount;
    }

    // API item 목록을 저장용 FoundVO 목록으로 변환
    private List<FoundVO> convertItems(
            List<FoundPortalApiItemVO> items,
            LocalDate today,
            LocalDate sixMonthsAgo) {

        List<FoundVO> foundList = new ArrayList<>();
        
        Set<String> insertedAtcIds = new HashSet<>();

        if (items == null || items.isEmpty()) {
            return foundList;
        }

        for (FoundPortalApiItemVO item : items) {
            FoundVO foundVO = convertToFoundVO(item, today, sixMonthsAgo);

            if (foundVO == null) {
                continue;
            }

            if (!insertedAtcIds.add(foundVO.getAtcId())) {
                continue;
            }

            foundList.add(foundVO);
        }

        return foundList;
    }

    // API item 1건을 FoundVO 1건으로 반환
    private FoundVO convertToFoundVO(
            FoundPortalApiItemVO item,
            LocalDate today,
            LocalDate sixMonthsAgo){

        if(item == null){
            return null;
        }

        if (item.getAtcId() == null || item.getAtcId().trim().isEmpty()) {
            return null;
        }
    
        if (item.getFdYmd() == null || item.getFdYmd().trim().isEmpty()) {
            return null;
        }

        String atcId = item.getAtcId().trim();

        LocalDate fdYmd;

        try{
            fdYmd = LocalDate.parse(item.getFdYmd().trim());
        }catch (Exception e){
            System.out.println("날짜 피싱 실패 fdYmd = " + item.getFdYmd());
            return null;
        }

        if(fdYmd.isBefore(sixMonthsAgo) || fdYmd.isAfter(today)){
            return null;
        }

        FoundVO foundVO = new FoundVO();

        foundVO.setAtcId(atcId);
        foundVO.setClrNm(item.getClrNm());
        foundVO.setDepPlace(item.getDepPlace());
        foundVO.setFdFilepathImg(item.getFdFilepathImg());
        foundVO.setFdPrdtNm(item.getFdPrdtNm());
        foundVO.setFdSbjt(item.getFdSbjt());
        foundVO.setFdYmd(fdYmd.atStartOfDay());
        
        // 카테고리 기본형태 대분류>소분류 임시저장
        String originalPrdtClNm = item.getPrdtClNm(); 

        // 대분류
        foundVO.setPrdtClNm(getCategoryLabel(originalPrdtClNm)); 

        // 소분류
        foundVO.setPrdtCategory(getSubCategoryLabel(originalPrdtClNm)); 

        // 제목(물품명)에 '완료'가 있으면 완료 처리
        boolean isDone = hasCompleteMark(item.getFdPrdtNm());

        foundVO.setDone((isDone ? 1 : 0));

        return foundVO;
    }

    @Transactional
    public int replaceFoundPoliceItems(List<FoundPortalApiItemVO> items, LocalDate today, LocalDate sixMonthsAgo) {

        foundPoliceMapper.deleteAllFoundPolice();

        int insertCount = 0;

        Set<String> insertedAtcIds = new HashSet<>();

        for (FoundPortalApiItemVO item : items) {

            if (item.getAtcId() == null || item.getAtcId().trim().isEmpty()) {
                continue;
            }

            String atcId = item.getAtcId().trim();

            if (!insertedAtcIds.add(atcId)) {
                continue;
            }

            if (item.getFdYmd() == null || item.getFdYmd().trim().isEmpty()) {
                continue;
            }

            LocalDate fdYmd;

            try {
                fdYmd = LocalDate.parse(item.getFdYmd());
            } catch (Exception e) {
                System.out.println("날짜 파싱 실패 fdYmd = " + item.getFdYmd());
                continue;
            }

            if (fdYmd.isBefore(sixMonthsAgo) || fdYmd.isAfter(today)) {
                continue;
            }

            FoundVO foundVO = new FoundVO();

            // 기존 FoundPoliceServiceImpl 매핑 그대로 적용
            foundVO.setAtcId(atcId);
            foundVO.setId(null);

            foundVO.setClrNm(item.getClrNm());
            foundVO.setDepPlace(item.getDepPlace());
            foundVO.setFdFilepathImg(item.getFdFilepathImg());
            foundVO.setFdPrdtNm(item.getFdPrdtNm());
            foundVO.setFdSbjt(item.getFdSbjt());

            String originalPrdtClNm = item.getPrdtClNm();
            String categoryLabel = getCategoryLabel(originalPrdtClNm);
            foundVO.setPrdtClNm(categoryLabel);
            
            foundVO.setPrdtCategory(getSubCategoryLabel(originalPrdtClNm));

            foundVO.setFdYmd(fdYmd.atStartOfDay());

            // 기존 FoundPoliceServiceImpl 기준
            foundVO.setDone(0);
            foundVO.setIsDeleted(0);

            foundPoliceMapper.insertFoundPolice(foundVO);

            insertCount++;
        }

        return insertCount;
    }

    private boolean hasCompleteMark(String value) {
        if (value == null || value.trim().isEmpty()) {
           return false;
        }

        String text = value.trim();

        return text.contains("완료")
                || text.contains("연락");
    }   

    private String getCategoryLabel(String prdtClNm) {
        if (prdtClNm == null || prdtClNm.trim().isEmpty()) {
            return "기타";
        }

        // 1. ">" 구분자가 있으면 무조건 앞부분(대분류 파트)만 먼저 잘라냅니다.
        String mainPart = prdtClNm;
        if (prdtClNm.contains(">")) {
            mainPart = prdtClNm.split(">")[0].trim();
        } else {
            mainPart = prdtClNm.trim();
        }

        // 2. 잘라낸 앞부분 단어를 기준으로 프로젝트 9대 라벨링 분류 시작
        if (mainPart.contains("가방") || mainPart.contains("백")) {
            return "가방";
        }

        if (mainPart.contains("귀금속") ||
                mainPart.contains("반지") ||
                mainPart.contains("목걸이") ||
                mainPart.contains("귀걸이") ||
                mainPart.contains("시계")) {
            return "귀금속";
        }

        if (mainPart.contains("도서") ||
                mainPart.contains("책") ||
                mainPart.contains("서적") ||
                mainPart.contains("소설")) {
            return "도서";
        }

        if (mainPart.contains("의류") ||
                mainPart.contains("모자") ||
                mainPart.contains("신발")) {
            return "의류";
        }

        if (mainPart.contains("자동차") ||
                mainPart.contains("네비") ||
                mainPart.contains("번호판")) {
            return "자동차";
        }

        if (mainPart.contains("핸드폰") ||
                mainPart.contains("휴대폰") ||
                mainPart.contains("아이폰")) {
            return "핸드폰";
        }

        if (mainPart.contains("전자") ||
                mainPart.contains("노트북") ||
                prdtClNm.contains("컴퓨터")) { // 원본 텍스트 유연성 확보
            return "전자기기";
        }

        if (mainPart.contains("지갑")) {
            return "지갑";
        }

        if (mainPart.contains("카드")) {
            return "카드";
        }

        return "기타";
    }

    /**
     * [2단계: 소분류 완벽 분리 추출 엔지니어링]
     * "가방 > 남성용 가방"에서 정확히 뒷부분("남성용 가방")만 싹둑 잘라내 리턴합니다.
     */
    private String getSubCategoryLabel(String prdtClNm) {
        if (prdtClNm == null || prdtClNm.trim().isEmpty()) {
            return "기타";
        }

        // 1. ">" 구분자가 존재하면 확실하게 스플릿해서 2번째(인덱스 1번) 데이터를 가져옵니다.
        if (prdtClNm.contains(">")) {
            String[] parts = prdtClNm.split(">");
            if (parts.length > 1) {
                return parts[1].trim(); // 뒷부분 "남성용 가방"에서 공백 싹 지우고 순수 글자만 리턴
            }
        }

        // 2. 만약 ">" 없이 "서류" 처럼 단일 문자열로 들어왔는데 프로젝트 9대 대분류에 안 걸린다면,
        // 그 글자 자체가 소중한 힌트이므로 소분류 컬럼에 그대로 이식해 둡니다 (유실 방지)
        String mainCategory = getCategoryLabel(prdtClNm);
        if ("기타".equals(mainCategory)) {
            return prdtClNm.trim();
        }

        // 3. 구분자도 없고 대분류만 매칭되는 밋밋한 단어일 때는 기본값 세팅
        return "기타";
    }
}