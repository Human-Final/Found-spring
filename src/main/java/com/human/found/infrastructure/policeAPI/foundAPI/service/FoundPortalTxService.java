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

    private static final int BATCH_SIZE = 500;

    private final FoundPortalMapper foundPortalMapper;
    // private final FoundPoliceMapper foundPoliceMapper;

    /**
     * 포털기관 습득물 UPSERT
     */
    @Transactional
    public int upsertFoundPortalItems(
            List<FoundPortalApiItemVO> items,
            LocalDate today,
            LocalDate sixMonthsAgo) {

        long methodStartTime =
                System.currentTimeMillis();

        System.out.println(
            "FoundPortal 데이터 변환 시작"
            + " / API item 수 = "
            + (items == null ? 0 : items.size())
        );

        long convertStartTime =
                System.currentTimeMillis();

        List<FoundVO> foundList =
                convertItems(
                    items,
                    today,
                    sixMonthsAgo
                );

        long convertEndTime =
                System.currentTimeMillis();

        System.out.println(
            "FoundPortal 데이터 변환 완료"
            + " / 저장 대상 수 = " + foundList.size()
            + " / 변환 시간 = "
            + (convertEndTime - convertStartTime)
            + "ms"
        );

        if (foundList.isEmpty()) {
            System.out.println(
                "저장 가능한 포털기관 습득물 데이터가 없습니다."
            );

            return 0;
        }

        int saveCount = 0;
        int batchNumber = 0;

        long upsertStartTime =
                System.currentTimeMillis();

        for (int i = 0;
             i < foundList.size();
             i += BATCH_SIZE) {

            batchNumber++;

            int end =
                    Math.min(
                        i + BATCH_SIZE,
                        foundList.size()
                    );

            List<FoundVO> batchList =
                    foundList.subList(i, end);

            long batchStartTime =
                    System.currentTimeMillis();

            foundPortalMapper.upsertFoundPortal(
                batchList
            );

            long batchEndTime =
                    System.currentTimeMillis();

            saveCount += batchList.size();

            System.out.println(
                "FoundPortal UPSERT 진행"
                + " / 배치 번호 = " + batchNumber
                + " / 저장 범위 = "
                + (i + 1)
                + "~"
                + end
                + " / 전체 = " + foundList.size()
                + " / 배치 건수 = " + batchList.size()
                + " / 배치 시간 = "
                + (batchEndTime - batchStartTime)
                + "ms"
            );
        }

        long upsertEndTime =
                System.currentTimeMillis();

        System.out.println(
            "FoundPortal UPSERT 전체 완료"
            + " / 저장 건수 = " + saveCount
            + " / UPSERT 시간 = "
            + (upsertEndTime - upsertStartTime)
            + "ms"
        );

        long methodEndTime =
                System.currentTimeMillis();

        System.out.println(
            "FoundPortalTxService 전체 시간 = "
            + (methodEndTime - methodStartTime)
            + "ms"
        );

        return saveCount;
    }

    /**
     * 모든 API 페이지가 정상 처리된 뒤에만 오래된 데이터를 논리삭제합니다.
     */
    @Transactional
    public int markOldFoundPortalDeleted(
            LocalDate sixMonthsAgo) {

        return foundPortalMapper
                .markOldFoundPortalDeleted(
                    sixMonthsAgo.atStartOfDay()
                );
    }

    /**
     * API Item 목록을 FoundVO 목록으로 변환
     */
    private List<FoundVO> convertItems(
            List<FoundPortalApiItemVO> items,
            LocalDate today,
            LocalDate sixMonthsAgo) {

        List<FoundVO> foundList =
                new ArrayList<>();

        Set<String> insertedAtcIds =
                new HashSet<>();

        if (items == null || items.isEmpty()) {
            return foundList;
        }

        for (FoundPortalApiItemVO item : items) {

            FoundVO foundVO =
                    convertToFoundVO(
                        item,
                        today,
                        sixMonthsAgo
                    );

            if (foundVO == null) {
                continue;
            }

            if (!insertedAtcIds.add(
                    foundVO.getAtcId())) {

                continue;
            }

            foundList.add(foundVO);
        }

        return foundList;
    }

    /**
     * API Item 1건을 FoundVO 1건으로 변환
     */
    private FoundVO convertToFoundVO(
            FoundPortalApiItemVO item,
            LocalDate today,
            LocalDate sixMonthsAgo) {

        if (item == null) {
            return null;
        }

        if (item.getAtcId() == null ||
            item.getAtcId()
                .trim()
                .isEmpty()) {

            return null;
        }

        if (item.getFdYmd() == null ||
            item.getFdYmd()
                .trim()
                .isEmpty()) {

            return null;
        }

        String atcId =
                item.getAtcId().trim();

        LocalDate fdYmd;

        try {
            fdYmd = LocalDate.parse(
                item.getFdYmd().trim()
            );

        } catch (Exception e) {
            System.out.println(
                "날짜 파싱 실패"
                + " / atcId = " + atcId
                + " / fdYmd = " + item.getFdYmd()
            );

            return null;
        }

        if (fdYmd.isBefore(sixMonthsAgo) ||
            fdYmd.isAfter(today)) {

            return null;
        }

        FoundVO foundVO = new FoundVO();

        foundVO.setAtcId(atcId);
        foundVO.setClrNm(item.getClrNm());
        foundVO.setDepPlace(item.getDepPlace());
        foundVO.setFdFilepathImg(
            item.getFdFilepathImg()
        );
        foundVO.setFdPrdtNm(item.getFdPrdtNm());
        foundVO.setFdSbjt(item.getFdSbjt());
        foundVO.setFdYmd(fdYmd.atStartOfDay());

        String originalPrdtClNm =
                item.getPrdtClNm();

        foundVO.setPrdtClNm(
            getCategoryLabel(originalPrdtClNm)
        );

        foundVO.setPrdtCategory(
            getSubCategoryLabel(originalPrdtClNm)
        );

        boolean isDone =
                hasCompleteMark(
                    item.getFdPrdtNm()
                );

        foundVO.setDone(isDone ? 1 : 0);
        foundVO.setIsDeleted(0);

        return foundVO;
    }

    // /**
    //  * 경찰청 습득물 전체 교체
    //  */
    // @Transactional
    // public int replaceFoundPoliceItems(
    //         List<FoundPortalApiItemVO> items,
    //         LocalDate today,
    //         LocalDate sixMonthsAgo) {

    //     foundPoliceMapper.deleteAllFoundPolice();

    //     int insertCount = 0;

    //     Set<String> insertedAtcIds =
    //             new HashSet<>();

    //     for (FoundPortalApiItemVO item : items) {

    //         if (item == null) {
    //             continue;
    //         }

    //         if (item.getAtcId() == null ||
    //             item.getAtcId()
    //                 .trim()
    //                 .isEmpty()) {

    //             continue;
    //         }

    //         String atcId =
    //                 item.getAtcId().trim();

    //         if (!insertedAtcIds.add(atcId)) {
    //             continue;
    //         }

    //         if (item.getFdYmd() == null ||
    //             item.getFdYmd()
    //                 .trim()
    //                 .isEmpty()) {

    //             continue;
    //         }

    //         LocalDate fdYmd;

    //         try {
    //             fdYmd = LocalDate.parse(
    //                 item.getFdYmd().trim()
    //             );

    //         } catch (Exception e) {
    //             System.out.println(
    //                 "날짜 파싱 실패"
    //                 + " / atcId = " + atcId
    //                 + " / fdYmd = "
    //                 + item.getFdYmd()
    //             );

    //             continue;
    //         }

    //         if (fdYmd.isBefore(sixMonthsAgo) ||
    //             fdYmd.isAfter(today)) {

    //             continue;
    //         }

    //         FoundVO foundVO = new FoundVO();

    //         foundVO.setAtcId(atcId);
    //         foundVO.setId(null);
    //         foundVO.setClrNm(item.getClrNm());
    //         foundVO.setDepPlace(
    //             item.getDepPlace()
    //         );
    //         foundVO.setFdFilepathImg(
    //             item.getFdFilepathImg()
    //         );
    //         foundVO.setFdPrdtNm(
    //             item.getFdPrdtNm()
    //         );
    //         foundVO.setFdSbjt(
    //             item.getFdSbjt()
    //         );

    //         String originalPrdtClNm =
    //                 item.getPrdtClNm();

    //         foundVO.setPrdtClNm(
    //             getCategoryLabel(
    //                 originalPrdtClNm
    //             )
    //         );

    //         foundVO.setPrdtCategory(
    //             getSubCategoryLabel(
    //                 originalPrdtClNm
    //             )
    //         );

    //         foundVO.setFdYmd(
    //             fdYmd.atStartOfDay()
    //         );
    //         foundVO.setDone(0);
    //         foundVO.setIsDeleted(0);

    //         foundPoliceMapper.insertFoundPolice(
    //             foundVO
    //         );

    //         insertCount++;
    //     }

    //     return insertCount;
    // }

    private boolean hasCompleteMark(
            String value) {

        if (value == null ||
            value.trim().isEmpty()) {

            return false;
        }

        String text = value.trim();

        return text.contains("완료") ||
               text.contains("연락");
    }

    private String getCategoryLabel(
            String prdtClNm) {

        if (prdtClNm == null ||
            prdtClNm.trim().isEmpty()) {

            return "기타";
        }

        String mainPart;

        if (prdtClNm.contains(">")) {
            mainPart =
                    prdtClNm
                        .split(">")[0]
                        .trim();
        } else {
            mainPart = prdtClNm.trim();
        }

        if (mainPart.contains("가방") ||
            mainPart.contains("백")) {

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
            prdtClNm.contains("컴퓨터")) {

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

    private String getSubCategoryLabel(
            String prdtClNm) {

        if (prdtClNm == null ||
            prdtClNm.trim().isEmpty()) {

            return "기타";
        }

        if (prdtClNm.contains(">")) {

            String[] parts =
                    prdtClNm.split(">");

            if (parts.length > 1) {
                return parts[1].trim();
            }
        }

        String mainCategory =
                getCategoryLabel(prdtClNm);

        if ("기타".equals(mainCategory)) {
            return prdtClNm.trim();
        }

        return "기타";
    }
}
