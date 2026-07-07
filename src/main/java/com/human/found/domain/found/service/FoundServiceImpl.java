package com.human.found.domain.found.service;

import java.util.List;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.human.found.domain.found.mapper.FoundFileMapper;
import com.human.found.domain.found.mapper.FoundMapper;
import com.human.found.domain.found.vo.FoundFileVO;
import com.human.found.domain.found.vo.FoundVO;
import com.human.found.domain.user.mapper.UserMapper;
import com.human.found.domain.user.vo.UserVO;
import com.human.found.global.common.paging.PagingVO;
import com.human.found.infrastructure.file.FileUtil;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class FoundServiceImpl implements FoundService {

    private final FoundMapper foundMapper;
    private final FoundFileMapper foundfilemapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    // 공통 파일 유틸 주입
    private final FileUtil fileUtil;

    @Transactional
    @Override
    public void Register(FoundVO foundVO, MultipartFile[] files) {

        // 상품분류 기본값 처리
        if (foundVO.getPrdtClNm() == null || foundVO.getPrdtClNm().isEmpty()) {
            foundVO.setPrdtClNm("기타물품");
        }
        // 게시글 저장
        foundMapper.insertfound(foundVO);
        // 게시글 번호 가져오기
        String foundid = foundVO.getAtcId();
        // 복잡했던 파일 업로드 & 물리 저장을 유틸 호출 한 줄로 대체

        // 파운드 영역이므로 맨 뒤 구분값에 "found"를 던져줍니다.
        List<Map<String, Object>> uploadedMaps = fileUtil.uploadFiles(files, foundid, "found");

        // 이미지..
        if (uploadedMaps != null && !uploadedMaps.isEmpty()) {
            // 업로드된 파일 리스트 중 "0번째(첫 번째) 파일 이름"을 꺼냅니다
            String saveFileName=(String) uploadedMaps.get(0).get("saveFileName");

            // 파일 경로를 foundVO의 대표이미지 공간에 세팅
            foundVO.setFdFilepathImg("/images/found/" + saveFileName);

            // System.out.println("atcId = " + foundVO.getAtcId());
            // System.out.println("대표이미지 = " + foundVO.getFdFilepathImg());


            foundMapper.updateThumbnail(foundVO);

            // 유틸이 뱉은 만능 맵(Map)에서 꺼내 내 FoundFileVO로 새로 포장하기
            for (Map<String, Object> info : uploadedMaps) {
                FoundFileVO fileVO = new FoundFileVO();
                fileVO.setFdFilepathImg((String) info.get("originalname"));// Object강제 형변환(String)
                fileVO.setAtcId((String) info.get("atcId"));
                fileVO.setSaveName((String) info.get("saveFileName"));
                fileVO.setFilePath((String) info.get("filePath"));

                //용량체크
                if(info.get("fileSize")!=null){
                    fileVO.setFileSize(Long.parseLong(String.valueOf(info.get("fileSize"))));
                }

                // DB저장
                foundfilemapper.insertFile(fileVO);
            }
        }
    }

    // 조회
    @Override
    public List<FoundVO> getFoundList(PagingVO pagingVO) {
        long totalCount = foundMapper.countFoundList();
        pagingVO.pageInfo((int) totalCount);
        return foundMapper.selectFoundList(pagingVO);
    }

    // 조회 수 올리기
    @Override
    public void viewCountPlus(String atcId) {
        foundMapper.viewCountPlus(atcId);
    }

    // 검색
    @Override
    public List<FoundVO> searchFoundItems(List<String> category, String subCategory, String colorSelect, String startDate, String endDate, String author, String status, String keyword, String sort, PagingVO pagingVO) {
        
        // 데이터 정제 안전장치 가동
        if ("all".equalsIgnoreCase(status)) status = "";
        if (keyword != null) keyword = keyword.trim();
        else keyword = "";
        
        if (startDate == null || startDate.trim().isEmpty()) {
            startDate = "";
        } else {
            startDate = startDate.trim();
        }

        if (endDate == null || endDate.trim().isEmpty()) {
            endDate = "";
        } else {
            endDate = endDate.trim();
        }

        // System.out.println("🔄 [습득물 서비스단] 복합 검색 엔진 기동 -> 현재 페이지: " + pagingVO.getPage());

        // 매퍼 인터페이스로 모든 인자 토스
        return foundMapper.selectFoundSearchList(category, subCategory, colorSelect, startDate, endDate, author, status, keyword, sort, pagingVO);
    }

    // 페이징을 위한 조회결과 갯수 세기
    @Override
    public int getTotalSearchCount(List<String> category, String subCategory, String colorSelect, String startDate, String endDate, String author, String status, String keyword) {
        
        if ("all".equalsIgnoreCase(status)) status = "";
        if (keyword != null) keyword = keyword.trim();
        else keyword = "";
        
        if (startDate == null || startDate.trim().isEmpty()) {
            startDate = "";
        } else {
            startDate = startDate.trim();
        }

        if (endDate == null || endDate.trim().isEmpty()) {
            endDate = "";
        } else {
            endDate = endDate.trim();
        }

        // 매퍼의 카운트 전용 메서드 호출
        return foundMapper.selectFoundSearchCount(category, subCategory, colorSelect, startDate, endDate, author, status, keyword);
    }

    // 삭제
    @Transactional
    @Override
    public void deletefound(String atcId, String inputpw, String loginid, boolean isAdmin) {
        // 습득물 번호(atcId)으로 게시글 정보 가져오기

        FoundVO found = foundMapper.selectDetailatcId(atcId);
        // 글 존재여부
        if (found == null) {
            throw new RuntimeException("존재하지 않는 게시글");
        }

        // 권한별 검증 분기
        if (!isAdmin) {
            // 일반유저 검증

            // 외부 수집 데이터(id가 null인 것 포함)는 일반 유저가 절대 삭제 불가하도록 방어
            if (!"user".equals(found.getDataSource())) {
                throw new RuntimeException("공공데이터 삭제할수 없습니다");
            }

            // 본인이 쓴 글인지 검증(글에 저장된 작성자 id 와 로그인한 id 비교)
            if (found.getId() == null || !found.getId().equals(loginid)) {
                throw new RuntimeException("본인이 작성한 글만 삭제할 수 있습니다");
            }

            // 입력된 비밀번호(inputpw)가 db의 회원 비밀번호와 일치하는지
            if (inputpw == null || inputpw.trim().isEmpty()) {
                throw new RuntimeException("비밀번호를 입력해 주세요");
            }

            // 2. [공통] 회원 테이블에서 현재 로그인한 유저(또는 관리자) 정보 가져오기
            UserVO user = userMapper.findById(loginid);

            if (user == null) {
                throw new RuntimeException("회원 정보를 찾을 수 없습니다");
            }

            // 비밀번호 검증
            if (!passwordEncoder.matches(inputpw, user.getPw())) {
                throw new RuntimeException("비밀번호가 일치하지 않습니다.");
            }
        }
                
        // 폴더사진삭제
        if ("user".equals(found.getDataSource())) {
            List<FoundFileVO> fileList = foundfilemapper.findById(atcId);
            // 게시글 삭제
            foundMapper.FoundupdateDelete(atcId);
            foundfilemapper.deleteByAtcId(atcId);
            if (fileList != null && !fileList.isEmpty()) {
                for (FoundFileVO fileVO : fileList) {
                    fileUtil.deletePhysicalFile(fileVO.getSaveName(), "found");
                }
            }
            // foundMapper.FoundupdateDelete(atcId);
            // foundfilemapper.deleteByAtcId(atcId);
        } else if ("police".equals(found.getDataSource())) {
            foundMapper.PoliceDelete(atcId);
        } else if ("portal".equals(found.getDataSource())) {
            foundMapper.PortalDelete(atcId);
        } else {
            throw new RuntimeException("알수없는 DataSource:" + found.getDataSource());
        }
    }

    // 상세보기
    @Override
    public FoundVO foundgetdetail(String atcId) {
        // atcid 만 넘겨서 union 조회
        FoundVO foundVo = foundMapper.selectDetailatcId(atcId);
        if (foundVo == null) {
            throw new RuntimeException("게시글이 존재하지 않습니다");
        }
        if ("user".equals(foundVo.getDataSource())) {
            List<FoundFileVO> fileList = foundfilemapper.findById(foundVo.getAtcId());

            List<FoundFileVO> validFileList = fileList.stream()
                .filter(file -> file.getSaveName() != null && !file.getSaveName().isBlank())
                .toList();
            foundVo.setFileList(validFileList);
        }

        // 정상조회시 반환
        return foundVo;
    }

    // 댓글
    @Override
    public FoundVO getFoundByNum(Long num) {
        return foundMapper.getFoundByNum(num);
    }

    // 게시글 수정
    @Transactional
    @Override
    public void UpdateFound(FoundVO foundVO, MultipartFile[] newFiles, List<String> deleteFiles) {

        // 글 정보 수정
        foundMapper.updateFound(foundVO);

        // 기존 파일 삭제(DELETE) ->체크 한거 있을때만 동작
        if (deleteFiles != null && !deleteFiles.isEmpty()) {
            for (String saveName : deleteFiles) {
                
                foundfilemapper.deleteBySaveName(saveName);
                fileUtil.deletePhysicalFile(saveName, "found");
            }
        }

        // 새로 첨부한 사진이 있으면 추가로 저장

        if (newFiles != null && newFiles.length > 0 && !newFiles[0].isEmpty()) {
            List<Map<String, Object>> uploadMaps = fileUtil.uploadFiles(newFiles, foundVO.getAtcId(), "found");

            if (uploadMaps != null && !uploadMaps.isEmpty()) {

                //새 파일 중 0번째 파일 이름을 가져와서 대표 이미지로 세팅합니다
                String saveFileName = (String) uploadMaps.get(0).get("saveFileName");
                foundVO.setFdFilepathImg("/images/found/" + saveFileName);

                foundMapper.updateThumbnail(foundVO);

                for (Map<String, Object> info : uploadMaps) {
                    FoundFileVO fileVO = new FoundFileVO();
                    fileVO.setAtcId(foundVO.getAtcId());
                    fileVO.setFdFilepathImg((String) info.get("originalname"));
                    fileVO.setSaveName((String) info.get("saveFileName"));
                    fileVO.setFilePath((String) info.get("filePath"));

                    // 파일 용량 정보 누락 확인
                    if (info.get("fileSize") != null) {
                        fileVO.setFileSize(Long.parseLong(String.valueOf(info.get("fileSize"))));
                    }
                    foundfilemapper.insertFile(fileVO);
                }
            }
        }

    }

}
