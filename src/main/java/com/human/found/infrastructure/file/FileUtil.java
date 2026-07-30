package com.human.found.infrastructure.file;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;


@Component
public class FileUtil {
    //상위 폴더
    private final String UPLOAD_PATH = "//192.168.0.53/260126/0608/배민선, 박상화, 김태연, 신민철/file/";
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        "jpg", "jpeg", "png", "gif", "webp",
        "mp4", "webm", "mov", "m4v", "avi"
    );

    private void validateExtension(String originalFilename){
        if(originalFilename == null || originalFilename.isBlank()){
            throw new IllegalArgumentException(
                "파일명이 없는 파일은 업로드될 수 없습니다."
            );
        }

        int dotIndex = originalFilename.lastIndexOf(".");

        if(dotIndex < 0 || dotIndex == originalFilename.length() - 1){
            throw new IllegalArgumentException(
                "확장자가 없는 파일은 업로드할 수 없습니다."
            );
        }

        String extension = originalFilename
            .substring(dotIndex + 1)
            .toLowerCase(Locale.ROOT);

        if(!ALLOWED_EXTENSIONS.contains(extension)){
            throw new IllegalArgumentException(
                originalFilename 
                    + " 파일은 업로드할 수 없습니다. " 
                    + "이미지 또는 동영상만 업로드해 주세요."
            );
        }
    }

    public List<Map<String, Object>> uploadFiles(
            MultipartFile[] files, 
            String atcId, 
            String type) {
                
        List<Map<String, Object>> fileList = new ArrayList<>();

        if (files == null || files.length == 0) {
            return fileList;
        }

        // 모든 파일을 먼저 검증
        for (MultipartFile file : files){
            if(file != null && !file.isEmpty()){
                validateExtension(file.getOriginalFilename());
            }
        }

        // 2. 입력받은 type("lost" 혹은 "found")에 따라 최종 경로를 완성합니다.
        String finalUploadPath = UPLOAD_PATH + type + "/";

        File folder = new File(finalUploadPath);

        // lost 또는 found 폴더가 없으면 자동으로 생성함
        if (!folder.exists() && !folder.mkdirs()) {
            throw new IllegalStateException(
                "파일 저장 폴더를 생성할 수 없습니다."
            );
        }

        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
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
