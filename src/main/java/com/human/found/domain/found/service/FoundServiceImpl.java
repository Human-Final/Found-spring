package com.human.found.domain.found.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.management.RuntimeErrorException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.human.found.domain.found.mapper.FoundFileMapper;
import com.human.found.domain.found.mapper.FoundMapper;
import com.human.found.domain.found.vo.FoundFileVO;
import com.human.found.domain.found.vo.FoundVO;
import com.human.found.domain.user.mapper.UserMapper;
import com.human.found.domain.user.vo.UserVO;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class FoundServiceImpl implements FoundService {

    private final FoundMapper foundMapper;
    private final FoundFileMapper foundfilemapper;
    private final UserMapper userMapper;

    @Transactional
    @Override
    public void Register(FoundVO foundVO,MultipartFile[]files) {

        if(foundVO.getPrdtClNm()==null||foundVO.getPrdtClNm().isEmpty()){
            foundVO.setPrdtClNm("기타물품");
        }

        foundMapper.insertfound(foundVO);
        Long foundid=foundVO.getNum();


        //파일 상자가 존재하고 첨부파일이 1개 이상일떄
        if(files !=null && files.length>0){
            String uploadPath="c:/upload/found/";//파일로컬 위치
            File folder = new File(uploadPath);
            if(!folder.exists()){
                folder.mkdirs(); // 폴더가 없으면 생성 
            }

            //사용자가 올린 배열을 하나씩 꺼내서 처리
            for(MultipartFile file:files){
                if(!file.isEmpty()){
                    String originalFileName=file.getOriginalFilename();//이미지 이름 원본
                    String uuid=UUID.randomUUID().toString();
                    String saveFileName=uuid+ "_" + originalFileName;//랜덤이름
                    try{
                        // 6. c:/upload/found/ 경로에 진짜 파일을 물리적으로 저장 (transferTo)
                        File saveFile = new File(uploadPath, saveFileName);
                        //실제 파일 저장
                        file.transferTo(saveFile);

                        FoundFileVO fileVo=new FoundFileVO();

                        fileVo.setFdFilepathImg(originalFileName);
                        fileVo.setBoardId(foundid);
                        fileVo.setSaveName(saveFileName);
                        fileVo.setFileSize(file.getSize());
                        fileVo.setFilePath("/upload/found/" + saveFileName);
                        
                        foundfilemapper.insertFile(fileVo);
                    }catch(IOException e){
                        e.printStackTrace();
                        throw new RuntimeException("파일 저장 중 오류가 발생하여 글 등록이 취소되었습니다.");
                    }
                }
            }
        }
        
    }
    //조회
    @Override
    public List<FoundVO> getFoundList() {
        return foundMapper.selectFoundList();
    }
    //삭제
    @Override
    public void deletefound(Long foundNum,String inputpw,String loginid) {
        // 습득물 번호(foundnum)으로 게시글 정보 가져오기
        
        FoundVO found= foundMapper.SelectFoundById(foundNum);
        // 글 존재여부 
        if(found==null){
            throw new RuntimeException("존재하지 않는 게시글");
        }
        // 본인이 쓴 글인지 검증(글에 저장된 작성자 id 와 로그인한 id 비교)
        if(!found.getId().equals(loginid)){
            throw new RuntimeException("본인이 작성한 글만 삭제할수 있습니다");
        }
        //member테이블에서 해당 회원정보 select문으로 불러오기
        UserVO user=userMapper.findById(loginid);
        if(user==null){
            throw new RuntimeException("회원 정보를 찾을수 없습니다");
        }
        //입력된 비밀번호(inputpw)가 db의 회원 비밀번호와 일치하는지
        if(!user.getPw().equals(inputpw)){
            throw new RuntimeException("비밀번호가 일치하지 않습니다");
        }
        foundMapper.FoundupdateDelete(foundNum);
    }

}
