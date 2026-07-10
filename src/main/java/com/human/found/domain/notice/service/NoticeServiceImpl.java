package com.human.found.domain.notice.service;

import java.io.File;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.human.found.domain.notice.mapper.NoticeMapper;
import com.human.found.domain.notice.vo.NoticeFileVO;
import com.human.found.domain.notice.vo.NoticeVO;
import com.human.found.global.common.paging.PagingVO;

@Service
public class NoticeServiceImpl implements NoticeService {

    private final NoticeMapper noticeMapper;
    private final String uploadPath = "\\\\192.168.0.53\\260126\\0608\\배민선, 박상화, 김태연, 신민철\\file\\notice\\"; // 서버 내 실제 파일 저장 경로

    public NoticeServiceImpl(NoticeMapper noticeMapper) {
        this.noticeMapper = noticeMapper;
    }

    @Override
    public List<NoticeVO> getNoticeList(PagingVO pagingVO) {
        int totalCount = noticeMapper.selectNoticeCount(pagingVO);
        pagingVO.pageInfo(totalCount);

        return noticeMapper.selectNoticeList(pagingVO); 
    }

    @Override
    public List<NoticeVO> getActivePopups() { 
        return noticeMapper.selectPopupNotices(); 
    }

    @Override
    @Transactional // 조회수 증가와 조회가 한 트랜잭션으로 묶임
    public NoticeVO getNoticeDetail(Long num) {
        noticeMapper.updateViewCount(num);
        return noticeMapper.selectNoticeDetail(num);
    }

    /**
     * 공지사항 등록
     */
    @Override
    @Transactional // 두 테이블에 동시 인서트 되므로 트랜잭션 보장 필수
    public void registerNotice(NoticeVO notice) {
        // 1. [순서 교정] 부모 공지사항 데이터를 먼저 DB에 저장합니다.
        // 실행이 완료되면 마이바티스에 의해 notice 객체의 num 필드에 자동 증가된 PK 번호가 채워집니다.
        noticeMapper.insertNotice(notice);
        
        // 2. 생성된 num 번호를 물고 파일 업로드 및 자식 테이블 인서트 로직을 수행합니다.
        handleFileUpload(notice);
    }

    /**
     * 공지사항 수정
     */
    @Override
    @Transactional
    public void modifyNotice(NoticeVO notice) {
        // 1. 공지사항 본문 데이터 업데이트
        noticeMapper.updateNotice(notice);
        
        // 2. 관리자가 수정 폼에서 새로운 이미지 파일을 업로드한 경우에만 교체 작업 진행
        if (notice.getUploadFile() != null && !notice.getUploadFile().isEmpty()) {
            NoticeFileVO oldNoticeFile = noticeMapper.selectNoticeFileByNum(notice.getNum());
        
            if (oldNoticeFile != null && oldNoticeFile.getFilePath() != null) {
                // [추가] 실제 폴더(디스크)에서 구 첨부파일을 영구 삭제합니다.
                deleteRealFile(oldNoticeFile.getFilePath());
            }

            // 1:1 유지를 위해 기존 첨부파일 데이터를 자식 테이블에서 완전히 삭제
            noticeMapper.deleteNoticeFile(notice.getNum());
            
            // 새 파일 저장 및 DB 등록
            handleFileUpload(notice);
        }
    }

    @Override
    @Transactional
    public void removeNotice(Long num, String imagePath) {
        if (imagePath != null && !imagePath.isBlank()){
            NoticeFileVO oldNoticeFile = noticeMapper.selectNoticeFileByNum(num);
            
            if (oldNoticeFile != null && oldNoticeFile.getFilePath() != null) {
                // [추가] 실제 폴더(디스크)에서 구 첨부파일을 영구 삭제합니다.
                deleteRealFile(oldNoticeFile.getFilePath());
            }
            noticeMapper.deleteNoticeFile(num);
        }
        noticeMapper.deleteNotice(num); 
    }

    /**
     * [개편 완료] 단일 이미지 파일의 명세 사양을 완벽히 추출하여 자식 테이블에 적재하는 공통 모듈
     */
    private void handleFileUpload(NoticeVO notice) {
        // 관리자가 파일을 실제로 첨부했을 때만 작동
        if (notice.getUploadFile() != null && !notice.getUploadFile().isEmpty()) {
            try {
                // 1) [교정 완료] 백슬래시 오작동을 차단한 최종 한글 공유 폴더 하위 디렉토리 타깃팅
                // String uploadPath = "\\\\192.168.0.53\\260126\\0608\\배민선, 박상화, 김태연, 신민철\\file\\notice\\";
                File folder = new File(uploadPath);
                if (!folder.exists() && !folder.mkdirs()) {
                    throw new IllegalStateException("공지사항 파일 저장 폴더를 생성할 수 없습니다.");
                }
                // 2) 이미지 이름 (fd_filepath_img) 추출
                String originalName = notice.getUploadFile().getOriginalFilename();
                
                // 3) 랜덤 이름 (save_name) 생성
                String saveName = java.util.UUID.randomUUID().toString() + "_" + originalName;
                
                // 4) 파일 크기 (file_size) 계산
                long fileSize = notice.getUploadFile().getSize();
                
                // 5) 파일 경로 (file_path) 가상 웹 주소 지정
                String filePath = "/images/notice/" + saveName;
                
                // 6) 실제 네트워크 공유 폴더 세부 경로 내부로 물리 파일 전송 저장 수행
                notice.getUploadFile().transferTo(new File(uploadPath + saveName));
                
                // 7) [교정 완료] 보내주신 NoticeFileVO 사양 그대로 1:1 결합 변수 바인딩 조립
                com.human.found.domain.notice.vo.NoticeFileVO fileVO = new com.human.found.domain.notice.vo.NoticeFileVO();
                fileVO.setFdFilepathImg(originalName); // 이미지 이름
                fileVO.setNum(notice.getNum());       // 공지사항 번호 (FK)
                fileVO.setSaveName(saveName);          // 랜덤 이름
                fileVO.setFileSize(fileSize);          // 파일 크기
                fileVO.setFilePath(filePath);          // 파일 경로
                
                // 8) 자식 테이블에 최종 인서트
                noticeMapper.insertNoticeFile(fileVO);
                
            } catch (Exception e) {
                    throw new IllegalStateException("공지사항 파일 업로드 중 문제가 발생했습니다.", e);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public NoticeVO getNoticeForEditandDelete(Long num) {
        return noticeMapper.selectNoticeDetail(num);
    }  
    
    //실제 저장 경로에서 파일을 물리적으로 삭제하는 메서드
    private void deleteRealFile(String imagePath) {
        try {
            // 1. 역슬래시 구조로 완벽하게 마감된 기본 네트워크 폴더 루트
            String uploadDir = "\\\\192.168.0.53\\260126\\0608\\배민선, 박상화, 김태연, 신민철\\file\\notice\\";
            
            if (imagePath == null || imagePath.trim().isEmpty()) {
                // System.out.println("[삭제 건너뜀] 기존 이미지 파일 경로 데이터가 null이거나 비어있습니다.");
                return;
            }

            // 2. 에러 로그 분석 결과: imagePath 내부에 "images/notice/"가 포함되어 있으므로 순수 파일명만 추출
            // 예: "images/notice/uuid_억울이.jpg" -> "uuid_억울이.jpg"만 쏙 뽑아냅니다.
            if (imagePath.contains("/") || imagePath.contains("\\")) {
                imagePath = imagePath.substring(
                    Math.max(imagePath.lastIndexOf("/"), imagePath.lastIndexOf("\\")) + 1
                );
            }
            
            // 3. [핵심]: URI 객체를 쓰지 않고, 윈도우가 해석할 수 있는 백슬래시 절대 경로 문자열로 강제 결합
            // 이렇게 하면 한글, 공백, 쉼표가 섞여 있어도 자바가 에러를 내지 않고 그대로 파일 시스템에 접근합니다.
            String totalFullPath = uploadDir + imagePath;
            
            // 4. 자바 전통의 물리 파일 객체 생성
            java.io.File file = new java.io.File(totalFullPath);
            
            // System.out.println("=========================================");
            // System.out.println("[강제 물리 삭제 시도 최종 경로]: " + totalFullPath);
            
            // 5. 물리 삭제 프로세스 작동
            if (file.exists()) {
                // 메모리 점유 방지를 위한 가비지 컬렉터 가동 후 삭제
                System.gc(); 
                    
                boolean deleted = file.delete();

                if (!deleted) {
                    throw new IllegalStateException("공지사항 첨부파일 삭제에 실패했습니다.");
                }
            } 
            
        } catch (IllegalStateException  e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("공지사항 첨부파일 삭제 중 문제가 발생했습니다.", e);
        }

    }

}

