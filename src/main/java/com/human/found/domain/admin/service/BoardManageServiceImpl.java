package com.human.found.domain.admin.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.human.found.domain.admin.mapper.BoardManageMapper;
import com.human.found.domain.admin.vo.AdminFoundVO;
import com.human.found.domain.admin.vo.AdminLostVO;
import com.human.found.domain.admin.vo.AdminSearchVO;
import com.human.found.domain.found.mapper.FoundFileMapper;
import com.human.found.domain.found.mapper.FoundMapper;
import com.human.found.domain.found.vo.FoundVO;
import com.human.found.domain.lost.mapper.LostFileMapper;
import com.human.found.domain.lost.mapper.LostMapper;
import com.human.found.domain.lost.vo.LostVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardManageServiceImpl implements BoardManageService{

    private final BoardManageMapper boardManageMapper;
    private final LostMapper lostMapper;
    private final FoundMapper foundMapper;
    private final LostFileMapper lostFileMapper;
    private final FoundFileMapper foundFileMapper;

    public boolean isSearchConditionEmpty(AdminSearchVO searchVO) {
        // 1. 검색어가 있는지 확인
        boolean hasKeyword = searchVO.getKeyword() != null && !searchVO.getKeyword().trim().isEmpty();
        
        // 2. 카테고리가 선택되었는지 확인 (체크박스 다중 선택이면 보통 List나 배열 형태입니다)
        // 예: searchVO.getCategories() 또는 변수명에 맞게 매칭하세요.
        boolean hasCategory = searchVO.getCategories() != null && !searchVO.getCategories().isEmpty();
        
        // 3. 상태(진행중/완료)가 선택되었는지 확인
        boolean hasStatus = searchVO.getDoneList() != null && !searchVO.getDoneList().isEmpty();
        
        // 4. 데이터출처(사용자/경찰/포털)가 선택되었는지 확인
        boolean hasSource = searchVO.getDataSources() != null && !searchVO.getDataSources().isEmpty();

        // 5. 등록일 날짜 지정이 되어있는지 확인 (예: startRegDate, endRegDate 등)
        boolean hasStartDate = searchVO.getStartDate() != null && !searchVO.getStartDate().trim().isEmpty();
        boolean hasEndDate = searchVO.getEndDate() != null && !searchVO.getEndDate().trim().isEmpty();

        // 6. 삭제 포함 체크박스가 체크 되었는지 확인
        boolean hasIncludeDeleted = searchVO.isIncludeDeleted();

        // 모든 조건이 다 false(없음)여야 "비어있는 최초 상태"입니다.
        // 하나라도 true가 있다면 사용자가 검색 조건을 넣은 것이므로 false를 반환합니다.
        return !(hasKeyword || hasCategory || hasStatus || hasSource || hasStartDate || hasEndDate || hasIncludeDeleted);
    }

    // 관리자 모드 분실물 선택 삭제
    @Override
    @Transactional
    public void deleteLostList(List<String> atcId) {

        boardManageMapper.deleteLostList(atcId);
    }

    // 관리자 모드 습득물 선택 삭제
    @Override
    @Transactional
    public void deleteFoundList(List<String> atcId) {
        for(String id:atcId){
            if(id.startsWith("U")){
                boardManageMapper.deleteFoundList(id);
            }
            else if(id.startsWith("F")){
                boardManageMapper.deleteFoundPoliceList(id);
            }
            else{
                boardManageMapper.deleteFoundPortalList(id);
            }
        }
    }

    // 관리자 분실물 완료 처리
    @Override
    public void completeLostList(List<String> atcId){
        for(String id:atcId){
            if(id.startsWith("U")){
                boardManageMapper.completeLostList(id);
            }
            else{
                boardManageMapper.completeLostPoliceList(id);
            }
        }
    }

    // 관리자 습득물 완료 처리
    @Override
    public void completeFoundList(List<String> atcId){
        for(String id:atcId){
            if(id.startsWith("U")){
                boardManageMapper.completeFoundList(id);
            }
            else if(id.startsWith("F")){
                boardManageMapper.completeFoundPoliceList(id);
            }
            else{
                boardManageMapper.completeFoundPortalList(id);
            }
        }
    }


    // 관리자 분실물 검색 + 페이징 조회
    @Override
    public List<AdminLostVO> searchLostPage(AdminSearchVO searchVO) {
        return boardManageMapper.searchLostPage(searchVO);
    }

    // 관리자 분실물 검색 결과 개수 조회
    @Override
    public int countSearchLost(AdminSearchVO searchVO) {
        return boardManageMapper.countSearchLost(searchVO);
    }

    // 관리자 습득물 검색 + 페이징 조회
    @Override
    public List<AdminFoundVO> searchFoundPage(AdminSearchVO searchVO) {
        return boardManageMapper.searchFoundPage(searchVO);
    }

    // 관리자 습득물 검색 결과 개수 조회
    @Override
    public int countSearchFound(AdminSearchVO searchVO) {
        return boardManageMapper.countSearchFound(searchVO);
    }


    // 관리자 게시글 분실물 삭제 미리보기
    @Override
    public LostVO adminLostDetail(String atcId) {
        
        LostVO lostVO = boardManageMapper.selectAdminlostDetailAtcId(atcId);

        if(lostVO == null){
            throw new RuntimeException("게시글이 존재하지 않습니다.");
        }

        if("user".equals(lostVO.getDataSource())){
            lostVO.setFileList(
                lostFileMapper.findById(lostVO.getAtcId())
            );
        }
        return lostVO;
    }

    @Override
    public FoundVO adminFoundDetail(String atcId) {
        
        FoundVO foundVO=boardManageMapper.selectAdminfoundDetailAtcId(atcId);

        if(foundVO == null){
            throw new RuntimeException("게시글이 존재하지 않습니다.");
        }

        if("user".equals(foundVO.getDataSource())){
            foundVO.setFileList(
                foundFileMapper.findById(atcId));
        }
        return foundVO;
    }
    
    // 1. 관리자 분실물 게시글 엑셀 다운로드 구현
    @Override
    public void generateLostExcel(AdminSearchVO searchVO, OutputStream outputStream) {
        // [핵심] LIMIT 조건에 잘리지 않도록 페이징 제한을 풀고 첫 페이지로 고정
        searchVO.setSize(999999);
        searchVO.setPage(1);

        // 기존에 목록 검색할 때 사용하던 매퍼 메서드 그대로 호출
        List<LostVO> list = boardManageMapper.selectLostListForExcel(searchVO);
        
        // 엑셀 생성 및 파일 쓰기
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("분실물 목록");

            // 헤더 정의
            Row headerRow = sheet.createRow(0);
            String[] columns = {"번호", "관리번호", "작성자ID", "분실장소", "물품명", "내용", "분실일", "대분류", "소분류", "등록일", "상태", "데이터출처"};
            
            CellStyle headerCellStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerCellStyle.setFont(headerFont);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            // LostVO 필드 매핑
            int rowNum = 1;
            for (LostVO vo : list) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(vo.getNum() != null ? vo.getNum() : 0L);
                row.createCell(1).setCellValue(vo.getAtcId() != null ? vo.getAtcId() : "");
                row.createCell(2).setCellValue(vo.getId() != null ? vo.getId() : "");
                row.createCell(3).setCellValue(vo.getLstPlace() != null ? vo.getLstPlace() : "");
                row.createCell(4).setCellValue(vo.getLstPrdtNm() != null ? vo.getLstPrdtNm() : "");
                row.createCell(5).setCellValue(vo.getLstSbjt() != null ? vo.getLstSbjt() : "");
                row.createCell(6).setCellValue(vo.getLstYmd() != null ? String.valueOf(vo.getLstYmd()) : "");
                
                // [핵심] 대분류(prdt_cl_nm)와 소분류(prdt_category) 데이터 출력 세팅
                row.createCell(7).setCellValue(vo.getPrdtClNm() != null ? vo.getPrdtClNm() : "");       // 대분류
                row.createCell(8).setCellValue(vo.getPrdtCategory() != null ? vo.getPrdtCategory() : ""); // 소분류
                
                row.createCell(9).setCellValue(vo.getCreatedAt() != null ? String.valueOf(vo.getCreatedAt()) : "");
                row.createCell(10).setCellValue(vo.getDone() != null && vo.getDone() == 0 ? "진행중" : "완료");
                row.createCell(11).setCellValue(vo.getDataSource() != null ? vo.getDataSource() : "");
            }

            // 셀 너비 자동 맞춤
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
        } catch (IOException e) {
            throw new RuntimeException("분실물 엑셀 생성 중 오류가 발생했습니다.", e);
        }
    }

    // 2. 관리자 습득물 게시글 엑셀 다운로드 구현
    @Override
    public void generateFoundExcel(AdminSearchVO searchVO, OutputStream outputStream) {
        // LIMIT 조건 제한 해제
        searchVO.setSize(999999);
        searchVO.setPage(1);

        // 기존에 습득물 목록 검색할 때 사용하던 매퍼 메서드 그대로 호출
        List<FoundVO> list = boardManageMapper.selectFoundListForExcel(searchVO);
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("습득물 목록");

            Row headerRow = sheet.createRow(0);
            // 제공해주신 FoundVO 필드 구조에 맞춤형 헤더 구성
            String[] columns = {"번호", "관리번호", "작성자ID", "보관장소", "물품명", "내용", "습득일시", "대분류", "소분류", "등록일", "상태", "데이터출처"};
            
            CellStyle headerCellStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerCellStyle.setFont(headerFont);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowNum = 1;
            for (FoundVO vo : list) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(vo.getNum() != null ? vo.getNum() : 0L);
                row.createCell(1).setCellValue(vo.getAtcId() != null ? vo.getAtcId() : "");
                row.createCell(2).setCellValue(vo.getId() != null ? vo.getId() : "");
                row.createCell(3).setCellValue(vo.getDepPlace() != null ? vo.getDepPlace() : ""); 
                row.createCell(4).setCellValue(vo.getFdPrdtNm() != null ? vo.getFdPrdtNm() : "");  
                row.createCell(5).setCellValue(vo.getFdSbjt() != null ? vo.getFdSbjt() : "");    
                row.createCell(6).setCellValue(vo.getFdYmd() != null ? String.valueOf(vo.getFdYmd()) : ""); 
                
                // [핵심] 대분류와 소분류 값을 안전하게 매핑 (Null일 경우 빈 공백 처리)
                row.createCell(7).setCellValue(vo.getPrdtClNm() != null ? vo.getPrdtClNm() : "");       // 대분류 (의류, 귀금속 등)
                row.createCell(8).setCellValue(vo.getPrdtCategory() != null ? vo.getPrdtCategory() : ""); // 소분류 (현재는 [NULL]로 들어옴)
                
                row.createCell(9).setCellValue(vo.getCreatedAt() != null ? String.valueOf(vo.getCreatedAt()) : "");
                row.createCell(10).setCellValue(vo.getDone() != null && vo.getDone() == 0 ? "진행중" : "완료");
                row.createCell(11).setCellValue(vo.getDataSource() != null ? vo.getDataSource() : "");
            }

            // 셀 너비 자동 맞춤
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
        } catch (IOException e) {
            throw new RuntimeException("습득물 엑셀 생성 중 오류가 발생했습니다.", e);
        }
    }
    

}