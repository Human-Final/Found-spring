package com.human.found.infrastructure.file;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;


@Component
public class FileUtil {
    //상위 폴더
    // 기존 사내 네트워크 주소(//192.168.0.53/...)를 지우고 아래 리눅스 경로로 변경
    private final String UPLOAD_PATH = "/home/ubuntu/upload_images/";


    public List<Map<String, Object>> uploadFiles(MultipartFile[] files, String atcId, String type) {
        List<Map<String, Object>> fileList = new ArrayList<>();
        if (files == null || files.length == 0) {
            return fileList;
        }
        // 2. 입력받은 type("lost" 혹은 "found")에 따라 최종 경로를 완성합니다.
        String finalUploadPath = UPLOAD_PATH + type + "/";

        File folder = new File(finalUploadPath);
        if (!folder.exists()) {
            folder.mkdirs();// lost 또는 found 폴더가 없으면 자동으로 생성함
        }
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String originalFilename = file.getOriginalFilename();
                String uuid = UUID.randomUUID().toString();
                String saveFileName = uuid + "_" + originalFilename;

                try {
                    File saveFile = new File(finalUploadPath, saveFileName);
                    file.transferTo(saveFile);
                    // 특정 VO 대신 만능 주머니인 Map에 정보를 담기
                    Map<String, Object> fileInfo = new HashMap<>();
                    fileInfo.put("atcId", atcId);
                    fileInfo.put("saveFileName", saveFileName);
                    fileInfo.put("fileSize", file.getSize());
                    fileInfo.put("filePath", finalUploadPath + saveFileName);
                    fileInfo.put("originalname", originalFilename);

                    fileList.add(fileInfo);

                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException("파일 저장 중 오류가 발생했습니다");
                }

            }
        }
        return fileList;
    }
    
    //물리 삭제 메서드
    public void deletePhysicalFile(String saveName,String type){
        String finalUploadPath = UPLOAD_PATH + type + "/";
        File file=new File(finalUploadPath,saveName);
        if(file.exists()){
            file.delete();
        }
            

    }
}
