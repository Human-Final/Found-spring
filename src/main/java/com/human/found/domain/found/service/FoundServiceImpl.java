package com.human.found.domain.found.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

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

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class FoundServiceImpl implements FoundService {

    private final FoundMapper foundMapper;
    private final FoundFileMapper foundfilemapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

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

        // 파일 상자가 존재하고 첨부파일이 1개 이상일떄
        if (files != null && files.length > 0) {
            String uploadPath = "//192.168.0.53/260126/0608/found/file/found/";// 파일로컬 위치
            File folder = new File(uploadPath);
            if (!folder.exists()) {
                folder.mkdirs(); // 폴더가 없으면 생성
            }

            // 사용자가 올린 배열을 하나씩 꺼내서 처리
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String originalFileName = file.getOriginalFilename();// 이미지 이름 원본
                    String uuid = UUID.randomUUID().toString();
                    String saveFileName = uuid + "_" + originalFileName;// 랜덤이름
                    try {
                        // 6. c:/upload/found/ 경로에 진짜 파일을 물리적으로 저장 (transferTo)
                        File saveFile = new File(uploadPath, saveFileName);
                        // 실제 파일 저장
                        file.transferTo(saveFile);
                        // 파일정보 db저장
                        FoundFileVO fileVo = new FoundFileVO();

                        fileVo.setFdFilepathImg(originalFileName);
                        fileVo.setAtcId((foundid));
                        fileVo.setSaveName(saveFileName);
                        fileVo.setFileSize(file.getSize());
                        fileVo.setFilePath("//192.168.0.53/260126/0608/found/file/found/" + saveFileName);

                        foundfilemapper.insertFile(fileVo);
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException("파일 저장 중 오류가 발생하여 글 등록이 취소되었습니다.");
                    }
                }
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

    // 삭제
    @Transactional
    @Override
    public String deletefound(String atcId, String inputpw, String loginid, boolean isAdmin) {
        // 습득물 번호(foundnum)으로 게시글 정보 가져오기

        FoundVO found = foundMapper.selectDetailatcId(atcId);
        // 글 존재여부
        if (found == null) {
            throw new RuntimeException("존재하지 않는 게시글");
        }
        // 2. [공통] 회원 테이블에서 현재 로그인한 유저(또는 관리자) 정보 가져오기
        UserVO user = userMapper.findById(loginid);
        


        // member테이블에서 해당 회원정보 select문으로 불러오기
        if (user == null) {
            throw new RuntimeException("회원 정보를 찾을 수 없습니다");
        }

        // 권한별 검증 분기
        if (!isAdmin) {
            // 일반유저 검증

            //외부 수집 데이터(id가 null인 것 포함)는 일반 유저가 절대 삭제 불가하도록 방어
            if(!"user".equals(found.getDataSource())){
                throw new RuntimeException("공공데이터 삭제할수 없습니다");
            }



            // 본인이 쓴 글인지 검증(글에 저장된 작성자 id 와 로그인한 id 비교)
            if (!found.getId().equals(loginid)) {
                throw new RuntimeException("본인이 작성한 글만 삭제할수 있습니다");
            }
            // 입력된 비밀번호(inputpw)가 db의 회원 비밀번호와 일치하는지
            if (!passwordEncoder.matches(inputpw, user.getPw())) {
                throw new RuntimeException("비밀번호가 일치하지 않습니다");
            }
        } else {
            // 관리자 검증
            // 관리자는 다른 사람 글도 지울 수 있으므로 작성자 검증은 Pass!
            // 만약 관리자도 최소한 '관리자 본인의 비밀번호'를 입력해야 삭제
            if (!passwordEncoder.matches(inputpw, user.getPw())) {
                throw new RuntimeException("관리자의 비밀번호가 일치하지 않습니다");
            }
        }

        // 폴더사진삭제
        if ("user".equals(found.getDataSource())) {
            List<FoundFileVO> filelist = foundfilemapper.findById(atcId);
            if (filelist != null && !filelist.isEmpty()) {
                String uploadPath = "//192.168.0.53/260126/0608/found/file/found/";

                for (FoundFileVO fileVO : filelist) {
                    File file = new File(uploadPath, fileVO.getSaveName());
                    if (file.exists()) {
                        file.delete();
                    }
                }
            }

            // 게시글
            foundMapper.FoundupdateDelete(atcId);
            // 사진 db삭제
            foundfilemapper.deleteByAtcId(atcId);
            // 상세페이지 주소 위한 반환
        } else if ("police".equals(found.getDataSource())) {
            foundMapper.PoliceDelete(atcId);
        } else if ("portal".equals(found.getDataSource())) {
            foundMapper.PortalDelete(atcId);
        }
        return atcId;
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
            foundVo.setFileList(fileList);
        }

        // 정상조회시 반환
        return foundVo;
    }

}
