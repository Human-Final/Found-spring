package com.human.found.domain.lost.service;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.human.found.domain.found.vo.FoundFileVO;
import com.human.found.domain.lost.mapper.LostFileMapper;
import com.human.found.domain.lost.mapper.LostMapper;
import com.human.found.domain.lost.vo.LostFileVO;
import com.human.found.domain.lost.vo.LostVO;
import com.human.found.domain.user.mapper.UserMapper;
import com.human.found.domain.user.vo.UserVO;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class LostServiceImpl implements LostService {
    private final LostMapper lostMapper;
    private final LostFileMapper lostFileMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void LostRegister(LostVO lostVO, MultipartFile[] files) {

        if(lostVO.getPrdtClNm()==null||lostVO.getPrdtClNm().isEmpty()){
            lostVO.setPrdtClNm("기타물품");
        }
        
        // 게시글 인서트 (이후 lostVO에 atcId가 자동으로 채워짐)
        System.out.println("등록시작");
        lostMapper.insertLost(lostVO);
        System.out.println("등록완료");
        String atcid = lostVO.getAtcId();
        if (files != null && files.length > 0) {

            String uploadPath = "//192.168.0.53/260126/0608/found/file/lost/";// 파일위치
            File folder = new File(uploadPath);
            if (!folder.exists()) {
                folder.mkdirs();// 폴더가 없으면 생성
            }

            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String originalFilename = file.getOriginalFilename();
                    String uuid = UUID.randomUUID().toString();
                    String saveFileName = uuid + "_" + originalFilename;
                    try {
                        File saveFile = new File(uploadPath, saveFileName);

                        file.transferTo(saveFile);

                        LostFileVO fileVo = new LostFileVO();
                        fileVo.setFdFilepathImg(originalFilename);
                        fileVo.setAtcId(atcid);
                        fileVo.setSaveName(saveFileName);
                        fileVo.setFileSize(file.getSize());
                        fileVo.setFilePath("//192.168.0.53/260126/0608/found/file/lost/" + saveFileName);
                        lostFileMapper.insertFile(fileVo);

                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException("파일 저장 중 오류가 발생하여 글 등록이 취소되었습니다.");
                    }
                }

            }

        }
    }

    @Override
    public List<LostVO> getLostlist() {
        return lostMapper.selectLostList();
    }

    @Override
    public String deletelost(String inputpw, Long lostNum, String loginid) {
        //(lostnum)으로 게시글정보 가져오기
        
        LostVO lost=lostMapper.selectlostbyId(lostNum);
        //글 존재여부
        if(lost==null){
            throw new RuntimeException("존재하지않는 게시글");
        }
        //상세 페이지 주소위한
        String atcId=lost.getAtcId();
        // 본인이 쓴 글인지 검증(글에 저장된 작성자 id 와 로그인한 id 비교)
        if(!lost.getId().equals(loginid)){
            throw new RuntimeException("본인이 작성한 글만 삭제할수 있습니다");
        }
        //member테이블에서 해당 회원정보 select문으로 불러오기
        UserVO user=userMapper.findById(loginid);
        if(user==null){
            throw new RuntimeException("회원 정보를 찾을수 없습니다");
        }
        //입력된 비밀번호(inputpw)가 db의 회원 비밀번호와 일치하는지
        if(!passwordEncoder.matches(inputpw, user.getPw())){
            throw new RuntimeException("비밀번호가 일치하지 않습니다");
        }
        //폴더 사진 삭제
        List<LostFileVO>fileList =lostFileMapper.findById(atcId);
        if(fileList!=null&&!fileList.isEmpty()){
            String uploadPath="//192.168.0.53/260126/0608/found/file/lost/";
            for(LostFileVO fileVO :fileList){
                File file=new File(uploadPath,fileVO.getSaveName());
                if(file.exists()){
                    file.delete();
                }
            }
        }
        lostMapper.lostupdateDelte(lostNum);
        lostFileMapper.deleteByAtcId(atcId);
        //상세페이지
        return atcId;
    }

    //상세조회
    @Override
    public LostVO lostdetail(String atcId) {
        LostVO lostVO=lostMapper.selectDetailatcId(atcId);
        if(lostVO==null){
            throw new RuntimeException("게시글이 존재하지 않습니다");
        }
        if("user".equals(lostVO.getDataSource())){
            List<LostFileVO>filevo=lostFileMapper.findById(lostVO.getAtcId());
            lostVO.setFilelist(filevo);
        }
        return lostVO;
    }
    

}
