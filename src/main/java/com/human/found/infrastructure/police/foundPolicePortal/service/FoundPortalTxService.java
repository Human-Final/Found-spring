package com.human.found.infrastructure.police.foundPolicePortal.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.human.found.domain.found.mapper.FoundPoliceMapper;
import com.human.found.domain.found.vo.FoundVO;
import com.human.found.infrastructure.police.foundPolicePortal.mapper.FoundPortalMapper;
import com.human.found.infrastructure.police.foundPolicePortal.vo.FoundPortalApiItemVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FoundPortalTxService {

    private final FoundPortalMapper foundPortalMapper;
    private final FoundPoliceMapper foundPoliceMapper;

    @Transactional
    public int replaceFoundPortalItems(List<FoundPortalApiItemVO> items, LocalDate today, LocalDate sixMonthsAgo) {

        foundPortalMapper.deleteAllFoundPortal();

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

            foundVO.setAtcId(atcId);
            foundVO.setClrNm(item.getClrNm());
            foundVO.setDepPlace(item.getDepPlace());
            foundVO.setFdFilepathImg(item.getFdFilepathImg());
            foundVO.setFdPrdtNm(item.getFdPrdtNm());
            foundVO.setFdSbjt(item.getFdSbjt());
            foundVO.setFdYmd(fdYmd.atStartOfDay());
            foundVO.setPrdtClNm(getCategoryLabel(item.getPrdtClNm()));

            foundVO.setDone(hasCompleteMark(item.getFdSbjt()) ? 1 : 0);

            foundPortalMapper.insertFoundPortal(foundVO);

            insertCount++;
        }

        return insertCount;
    }

    @Transactional
    public int replaceFoundPoliceItems(List<FoundPortalApiItemVO> items, LocalDate today, LocalDate sixMonthsAgo) {

        // foundPoliceMapper.deleteAllFoundPolice();

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

            foundVO.setFdYmd(fdYmd.atStartOfDay());

            // 기존 FoundPoliceServiceImpl 기준
            foundVO.setDone(0);
            foundVO.setIsDeleted(0);

            foundPoliceMapper.insertFoundPolice(foundVO);

            insertCount++;
        }

        return insertCount;
    }

    private boolean hasCompleteMark(String title) {
        return title != null && title.contains("완료");
    }

    private String getCategoryLabel(String prdtClNm) {
        if (prdtClNm == null || prdtClNm.trim().isEmpty()) {
            return "기타";
        }

        if (prdtClNm.contains("가방") || prdtClNm.contains("백")) {
            return "가방";
        }

        if (prdtClNm.contains("귀금속") ||
                prdtClNm.contains("반지") ||
                prdtClNm.contains("목걸이") ||
                prdtClNm.contains("귀걸이") ||
                prdtClNm.contains("시계")) {
            return "귀금속";
        }

        if (prdtClNm.contains("도서") ||
                prdtClNm.contains("책") ||
                prdtClNm.contains("서적") ||
                prdtClNm.contains("소설")) {
            return "도서";
        }

        if (prdtClNm.contains("의류") ||
                prdtClNm.contains("모자") ||
                prdtClNm.contains("신발")) {
            return "의류";
        }

        if (prdtClNm.contains("자동차") ||
                prdtClNm.contains("네비") ||
                prdtClNm.contains("번호판")) {
            return "자동차";
        }

        if (prdtClNm.contains("핸드폰") ||
                prdtClNm.contains("휴대폰") ||
                prdtClNm.contains("아이폰")) {
            return "핸드폰";
        }

        if (prdtClNm.contains("전자") ||
                prdtClNm.contains("노트북") ||
                prdtClNm.contains("컴퓨터")) {
            return "전자기기";
        }

        if (prdtClNm.contains("카드")) {
            return "카드";
        }

        return "기타";
    }
}