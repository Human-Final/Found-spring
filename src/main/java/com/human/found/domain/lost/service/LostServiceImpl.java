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
        
        String atcid = lostVO.getAtcId();

        List<Map<String,Object>> uploadedMaps=fileUtil.uploadFiles(files, atcid, "lost");

        for(Map<String,Object> info:uploadedMaps){
            LostFileVO fileVO=new LostFileVO();
            fileVO.setFdFilepathImg((String)info.get("originalname"));//Object강제 형변환(String)
            fileVO.setAtcId((String)info.get("atcId"));
            fileVO.setSaveName((String)info.get("saveFileName"));
            fileVO.setFileSize((Long)info.get("fileSize"));
            fileVO.setFilePath((String)info.get("filePath"));
            
            //db저장
            lostFileMapper.insertFile(fileVO);
        }

        // if (files != null && files.length > 0) {

        //     String uploadPath = "//192.168.0.53/260126/0608/found/file/lost/";// 파일위치
        //     File folder = new File(uploadPath);
        //     if (!folder.exists()) {
        //         folder.mkdirs();// 폴더가 없으면 생성
        //     }

        //     for (MultipartFile file : files) {
        //         if (!file.isEmpty()) {
        //             String originalFilename = file.getOriginalFilename();
        //             String uuid = UUID.randomUUID().toString();
        //             String saveFileName = uuid + "_" + originalFilename;
        //             try {
        //                 File saveFile = new File(uploadPath, saveFileName);

        //                 file.transferTo(saveFile);

        //                 LostFileVO fileVo = new LostFileVO();
        //                 fileVo.setFdFilepathImg(originalFilename);
        //                 fileVo.setAtcId(atcid);
        //                 fileVo.setSaveName(saveFileName);
        //                 fileVo.setFileSize(file.getSize());
        //                 fileVo.setFilePath("//192.168.0.53/260126/0608/found/file/lost/" + saveFileName);
        //                 lostFileMapper.insertFile(fileVo);

        //             } catch (Exception e) {
        //                 e.printStackTrace();
        //                 throw new RuntimeException("파일 저장 중 오류가 발생하여 글 등록이 취소되었습니다.");
        //             }
        //         }

        //     }

        //}
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
            if(fileList != null && !fileList.isEmpty()){
                for(LostFileVO fileVO:fileList){
                    fileUtil.deletePhysicalFile(fileVO.getSaveName(),"lost");
                }
            }
            //게시글 삭제
            lostMapper.lostupdateDelte(atcId);
            //사진 db삭제
            lostFileMapper.deleteByAtcId(atcId);
        }else if("police".equals(lost.getDataSource())){
            //경찰청 삭제
            lostMapper.PoliceDelete(atcId);
        }else{
            throw new RuntimeException("알수없는 DataSource:"+lost.getDataSource());
        }


        // if ("user".equals(lost.getDataSource())) {
        //     List<LostFileVO> fileList = lostFileMapper.findById(atcId);
        //     if (fileList != null && !fileList.isEmpty()) {
        //         String uploadPath = "//192.168.0.53/260126/0608/found/file/lost/";
        //         for (LostFileVO fileVO : fileList) {
        //             File file = new File(uploadPath, fileVO.getSaveName());
        //             if (file.exists()) {
        //                 file.delete();
        //             }
        //         }
        //     }
        //     // 게시글 삭제
        //     lostMapper.lostupdateDelte(atcId);
        //     // 사진 db삭제
        //     lostFileMapper.deleteByAtcId(atcId);
        // } else if ("police".equals(lost.getDataSource())) {
        //     // 경찰청 삭제
        //     lostMapper.PoliceDelete(atcId);
        // }
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

}
