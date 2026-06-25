package com.human.found.domain.lost.service;

import java.util.List;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.human.found.domain.lost.mapper.LostFileMapper;
import com.human.found.domain.lost.mapper.LostMapper;
import com.human.found.domain.lost.vo.LostFileVO;
import com.human.found.domain.lost.vo.LostVO;
import com.human.found.domain.user.mapper.UserMapper;
import com.human.found.domain.user.vo.UserVO;
import com.human.found.infrastructure.file.FileUtil;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class LostServiceImpl implements LostService {
    private final LostMapper lostMapper;
    private final LostFileMapper lostFileMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final FileUtil fileUtil;

    @Override
    @Transactional
    public void LostRegister(LostVO lostVO, MultipartFile[] files) {

        if (lostVO.getPrdtClNm() == null || lostVO.getPrdtClNm().isEmpty()) {
            lostVO.setPrdtClNm("기타물품");
        }

        //게시글저장
        lostMapper.insertLost(lostVO);
        
        // 게시글 번호 가져오기
        String atcid = lostVO.getAtcId();

        // 복잡했던 파일 업로드 & 물리 저장을 유틸 호출 한 줄로 대체
        List<Map<String,Object>> uploadedMaps=fileUtil.uploadFiles(files, atcid, "lost");

        if(uploadedMaps!=null&& !uploadedMaps.isEmpty()){
            // 업로드된 파일 리스트 중 "0번째(첫 번째) 파일 이름"을 꺼냅니다
            String saveFileName=(String)uploadedMaps.get(0).get("saveFileName");

            // 파일 경로를 foundVO의 대표이미지 공간에 세팅
            lostVO.setLstFilepathImg("/images/lost/"+saveFileName);

            System.out.println("atcId = " + lostVO.getAtcId());
            System.out.println("대표이미지 = " + lostVO.getLstFilepathImg());

            lostMapper.updateThumbnail(lostVO);

            for(Map<String,Object> info:uploadedMaps){
            LostFileVO fileVO=new LostFileVO();
            
                fileVO.setFdFilepathImg((String)info.get("originalname"));//Object강제 형변환(String)
                fileVO.setAtcId((String)info.get("atcId"));
                fileVO.setSaveName((String)info.get("saveFileName"));
                fileVO.setFilePath((String)info.get("filePath"));
                
                //용량체크
                if(info.get("fileSize")!=null){
                    fileVO.setFileSize(Long.parseLong(String.valueOf(info.get("fileSize"))));
                }

            //db저장
                lostFileMapper.insertFile(fileVO);
            }
        }
    }

    @Override
    public List<LostVO> getLostlist() {
        return lostMapper.selectLostList();
    }

    @Override
    @Transactional
    public void deletelost(String inputpw, String atcId, String loginid, boolean isAdmin) {
        // (atcId)으로 게시글정보 가져오기

        LostVO lost = lostMapper.selectDetailAtcId(atcId);
        // 글 존재여부
        if (lost == null) {
            throw new RuntimeException("존재하지않는 게시글");
        }
        // 권한별 검증 분기
        if (!isAdmin) {
            // 일반유저 검증
            // 외부 수집 데이터(id가 null인 것 포함)는 일반 유저가 절대 삭제 불가하도록 방어
            if (!"user".equals(lost.getDataSource())) {
                throw new RuntimeException("공공데이터 삭제할수 없습니다");
            }
            // 본인이 쓴 글인지 검증(글에 저장된 작성자 id 와 로그인한 id 비교)
            if (lost.getId() == null || !lost.getId().equals(loginid)) {
                throw new RuntimeException("본인이 작성한 글만 삭제할 수 있습니다");
            }
            // 입력된 비밀번호(inputpw)가 db의 회원 비밀번호와 일치하는지
            if (inputpw == null || inputpw.trim().isEmpty()) {
                throw new RuntimeException("비밀번호를 입력해 주세요");
            }
            // [공통] 회원 테이블에서 현재 로그인한 유저(또는 관리자) 정보 가져오기
            UserVO user = userMapper.findById(loginid);

            if (user == null) {
                throw new RuntimeException("회원 정보를 찾을 수 없습니다");
            }

            if (!passwordEncoder.matches(inputpw, user.getPw())) {
                throw new RuntimeException("비밀번호가 일치하지 않습니다");
            }
        }

        // 폴더 사진 삭제
        if("user".equals(lost.getDataSource())){
            List<LostFileVO> fileList=lostFileMapper.findById(atcId);
            lostMapper.lostupdateDelte(atcId);
            lostFileMapper.deleteByAtcId(atcId);
            if(fileList != null && !fileList.isEmpty()){
                for(LostFileVO fileVO:fileList){
                    fileUtil.deletePhysicalFile(fileVO.getSaveName(),"lost");
                }
            }
            //게시글 삭제
            // lostMapper.lostupdateDelte(atcId);
            //사진 db삭제
            // lostFileMapper.deleteByAtcId(atcId);
        }else if("police".equals(lost.getDataSource())){
            //경찰청 삭제
            lostMapper.PoliceDelete(atcId);
        }else{
            throw new RuntimeException("알수없는 DataSource:"+lost.getDataSource());
        }
    }

    // 상세조회
    @Override
    public LostVO lostdetail(String atcId) {
        LostVO lostVO = lostMapper.selectDetailAtcId(atcId);
        if (lostVO == null) {
            throw new RuntimeException("게시글이 존재하지 않습니다");
        }
        if ("user".equals(lostVO.getDataSource())) {
            List<LostFileVO> filevo = lostFileMapper.findById(lostVO.getAtcId());
            lostVO.setFilelist(filevo);
        }
        return lostVO;
    }
    
    // 분실물 게시글 댓글
    @Override
    public LostVO getLostByNum(Long num) {
        return lostMapper.findByNum(num);
    }

    @Override
    public void UpdateLost(LostVO lostVO, MultipartFile[] newFiles, List<String> deletefiles) {
        // 글 정보 수정
        lostMapper.UpdateLost(lostVO);

        // 기존 파일 삭제 -체크한거 있을떄만
        if(deletefiles !=null && !deletefiles.isEmpty()){
            for(String saveName : deletefiles){
                lostFileMapper.deleteBySaveName(saveName);
                fileUtil.deletePhysicalFile(saveName, "lost");
            }
        }

        // 새로 첨부한 사진이 있으면 추가로 저장
        if (newFiles != null && newFiles.length > 0 && !newFiles[0].isEmpty()){
            List<Map<String, Object>> uploadMaps = fileUtil.uploadFiles(newFiles, lostVO.getAtcId(), "lost");
            
            if (uploadMaps != null && !uploadMaps.isEmpty()){
                //새 파일 중 0번째 파일 이름을 가져와서 대표 이미지로 세팅합니다
                //String saveFileName = (String) uploadMaps.get(0).get("saveFileName");
                //lostVO.setLstFilepathImg("/images/lost/"+saveFileName);

                //lostMapper.updateThumbnail(lostVO);

                for(Map<String, Object> info : uploadMaps){
                    LostFileVO fileVO=new LostFileVO();
                    fileVO.setAtcId(lostVO.getAtcId());
                    fileVO.setFdFilepathImg((String)info.get("originalname"));
                    fileVO.setSaveName((String)info.get("saveFileName"));
                    fileVO.setFilePath((String)info.get("filePath"));

                    //파일 정보누락 확인
                    if(info.get("fileSize")!=null){
                        fileVO.setFileSize(Long.parseLong(String.valueOf(info.get("fileSize"))));
                    }
                    lostFileMapper.insertFile(fileVO);
                }
            }

        }
        //최종 파일정보 조회
        List<LostFileVO> remainFiles=lostFileMapper.findById(lostVO.getAtcId());
        //대표 이미지 재설정
        if(remainFiles!=null&&!remainFiles.isEmpty()){
            lostVO.setLstFilepathImg("/images/lost/"+remainFiles.get(0).getSaveName());
        }else{
            //파일이 하나도 없으면 대표 이미지 제거
            lostVO.setLstFilepathImg(null);
        }
        lostMapper.updateThumbnail(lostVO);
    }

}
