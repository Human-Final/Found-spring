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
    private final String UPLOAD_PATH = "//192.168.0.53/260126/0608/found/file/";

    // 허용 타입
    private final List<String> ALLOWED_CONTENT_TYPES=List.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "video/mp4",
            "video/webm",
            "video/quicktime"
    );

    // 허용 확장자
    private static final List<String> ALLOWED_EXTENSIONS=List.of(
        "jpg", "jpeg", "png", "gif", "webp",
            "mp4", "webm", "mov"
    ) ;

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
                if(originalFilename==null||originalFilename.isBlank()){
                    throw new RuntimeException("파일명이 없습니다.");
                }

                String uuid = UUID.randomUUID().toString();
                String saveFileName = uuid + "_" + originalFilename;
                
                // 파일 확장자 검사
                String extension="";

                if(originalFilename.contains(".")){
                    extension=originalFilename.substring(originalFilename.lastIndexOf(".")+1)
                    .toLowerCase();
                }

                if(!ALLOWED_EXTENSIONS.contains(extension)){
                    throw new RuntimeException("이미지 또는 동영상만 업로드 가능합니다.");
                }

                 // ContentType 검사
                String contentType=file.getContentType();

                if(contentType==null|| !ALLOWED_CONTENT_TYPES.contains(contentType)){
                    throw new RuntimeException("허용되지 않는 파일 형식입니다.");
                }

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
