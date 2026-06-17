package com.human.found.domain.lost.service;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.human.found.domain.lost.mapper.LostFileMapper;
import com.human.found.domain.lost.mapper.LostMapper;
import com.human.found.domain.lost.vo.LostFileVO;
import com.human.found.domain.lost.vo.LostVO;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class LostServiceImpl implements LostService {
    private final LostMapper lostMapper;
    private final LostFileMapper lostFileMapper;

    @Override
    @Transactional
    public void LostRegister(LostVO lostVO, MultipartFile[] files) {

        if(lostVO.getPrdtClNm()==null||lostVO.getPrdtClNm().isEmpty()){
            lostVO.setPrdtClNm("기타물품");
        }
        
        // 게시글 인서트 (이후 lostVO에 atcId가 자동으로 채워짐)
        lostMapper.insertLost(lostVO);
        String atcid = lostVO.getAtcId();
        if (files != null && files.length > 0) {

            String uploadPath = "c:/upload/lost/";// 파일위치
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
                        fileVo.setFilePath("/upload/lost/" + saveFileName);
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

}
