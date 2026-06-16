package com.human.found.domain.found.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.human.found.domain.found.mapper.FoundFileMapper;
import com.human.found.domain.found.mapper.FoundMapper;
import com.human.found.domain.found.vo.FoundFileVO;
import com.human.found.domain.found.vo.FoundVO;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class FoundServiceImpl implements FoundService {
    private final FoundMapper foundMapper;
    private final FoundFileMapper foundfilemapper;

    @Override
    public void Register(FoundVO foundVO,MultipartFile[]files) {
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
                    String originallFileName=file.getOriginalFilename();//이미지 이름 원본
                    String uuid=UUID.randomUUID().toString();
                    String saveFileName=uuid+ "_" + originallFileName;//랜덤이름
                    try{
                        // 6. c:/upload/found/ 경로에 진짜 파일을 물리적으로 저장 (transferTo)
                        File saveFile = new File(uploadPath, saveFileName);
                        //실제 파일 저장
                        file.transferTo(saveFile);

                        FoundFileVO fileVo=new FoundFileVO();

                        fileVo.setFdFilepathImg(originallFileName);
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

    @Override
    public List<FoundVO> getFoundList() {
        return foundMapper.selectFoundList();
    }

}