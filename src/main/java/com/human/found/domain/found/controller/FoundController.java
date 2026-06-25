package com.human.found.domain.found.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.human.found.domain.comment.service.FoundCommentService;
import com.human.found.domain.comment.vo.CommentVO;
import com.human.found.domain.found.service.FoundService;
import com.human.found.domain.found.vo.FoundVO;
import com.human.found.global.common.paging.PagingVO;
import com.human.found.infrastructure.map.KakaoMapConfig;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;



@Controller
@AllArgsConstructor
public class FoundController {
    private final FoundService foundService;
    private final KakaoMapConfig kakaoMapConfig;
    private final FoundCommentService foundCommentService;
    

    //=====습득물 등록====
    //BindingResult는 Spring MVC에서 폼 데이터 바인딩 결과와 검증(Validation) 결과를 저장하는 객체
    @PostMapping("/api/found")
    public String postRegister(@Valid @ModelAttribute("foundVO") FoundVO foundVO, 
        BindingResult bindingResult, Model model,
        @RequestParam(value="files",required = false) MultipartFile[]files, Principal principal ) {
        
        // 유효성 검증 에러 발생 시, 작성 폼으로 리턴 (입력했던 값은 foundVO에 의해 유지됨)
        if(bindingResult.hasErrors()){
            model.addAttribute("writetype", "found");
            return "found/write";
        }    
        
        // 2. 작성자 아이디 자동 설정 (로그인 세션이 있으면 해당 ID, 없으면 테스트 계정)
        if (principal != null){
            foundVO.setId(principal.getName());
        }else{
            foundVO.setId("test_member");
        }
        
        // 고유한 관리 번호(일련번호) 생성 및 세팅 (USER-현재시간밀리초)
        foundVO.setAtcId("USER-" + System.currentTimeMillis());
        
        // 비즈니스 로직 수행 (DB 저장 및 파일 업로드 처리)
        foundService.Register(foundVO,files);
        return "redirect:/api/found";
    }

    //=======조회======
    @GetMapping("/api/found")
    public String getFoundList(
        Model model, 
        @RequestParam(name="page", defaultValue = "1") int page) {
        
        //  페이징 처리를 위한 VO 객체 생성 및 설정
        PagingVO pagingVO = new PagingVO();
        pagingVO.setPage(page);
        pagingVO.setSize(10);   // 한 페이지에 보여줄 게시글 수
        pagingVO.setPageBlock((10)); // 하단에 보여줄 페이지 블록 수 ([1] [2] ... [10])   

        //  서비스로부터 페이징이 적용된 게시글 목록을 가져옴
        List<FoundVO>getList = foundService.getFoundList(pagingVO);

        //  뷰(HTML)로 데이터를 전달하기 위해 Model에 담기
        model.addAttribute("paging", pagingVO);
        model.addAttribute("getList", getList);
        model.addAttribute("isEdit", false); // 작성 폼과 공유할 때 '수정 모드'가 아님을 표시
        // model.addAttribute("countTotalFound", countTotalFound);

        return "found/list";
    }
    
    //습득 작성 폼

    @GetMapping("/api/write")
    public String foundWriteForm(Model model) {

        // Thymeleaf 폼 바인딩을 위해 빈 VO 객체를 전달
        model.addAttribute("foundVO", new FoundVO());
        
        // 습득물(found)과 분실물(lost) 등의 폼을 공통으로 쓸 때 구분하기 위한 값
        model.addAttribute("writetype", "found");

        return "found/write";
    }
    
    //========삭제========
    //Authentication 유저의 상세 인증 정보 (권한 확인용)
    //RedirectAttributes  리다이렉트 시 일회성 데이터를 전달하기 위한 객체
    @DeleteMapping("/api/found/{atcId}")
    public String deletefound(
        @RequestParam(value = "password", required = false)String inputpw,
        @PathVariable("atcId")String atcId,
        Authentication authentication ,RedirectAttributes redirectAttributes) {
        
            
        //로그인체크
        if(authentication==null){
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/api/found";
        }
        String loginid =authentication.getName(); 
        //로그인한 유저의 권한 목록 중 관리자 권한 (ADMIN) 이 있는지 확인
        boolean isAdmin=authentication.getAuthorities().stream()
        .anyMatch(auth->auth.getAuthority().equals("ROLE_ADMIN")||auth.getAuthority().equals("ADMIN"));
        try {
            //서비스단에 글 id 입력번호, 로그인한 유저 id 를 넘겨받아 검증 및 삭제
            foundService.deletefound(atcId,inputpw,loginid,isAdmin);
            redirectAttributes.addFlashAttribute("Message", "삭제완료");
            
            return "redirect:/api/found";

        } catch (Exception e) {
            // 검증 실패 시 에러 메시지를 들고 원래 상세 페이지로 리턴
            redirectAttributes.addFlashAttribute("errorMessage",e.getMessage());

                return "redirect:/api/found/detail/"+atcId;
        }
    }
    
    //====상세보기=====
    @GetMapping("/api/found/detail/{atcId}")
    public String foundgetdetail(@PathVariable("atcId") String atcId, Model model) {

        // 게시글 상세 정보 조회
        FoundVO foundVO = foundService.foundgetdetail(atcId);

        //  해당 게시글에 달린 댓글 목록 조회 (글 번호와 데이터 출처 구분값을 인자로 사용)
        List<CommentVO> commentList =
                foundCommentService.getCommentsByNum(
                        foundVO.getNum(),
                        foundVO.getDataSource()
                );

        model.addAttribute("foundVO", foundVO);
        model.addAttribute("commentList", commentList);
        // 카카오 지도 API 호출에 필요한 JavaScript Key 전달
        model.addAttribute("kakaoMapJsKey", kakaoMapConfig.getJsKey());

        return "found/detail";
    }
    
    //======수정화면==
    @GetMapping("/api/found/detail/{atcId}/edit")
    public String FoundEditForm(@PathVariable("atcId")String atcId,Model model,Authentication authentication,
    RedirectAttributes redirectAttributes) {

        //  로그인 여부 확인
        if(authentication==null){
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다");
            return "redirect:/api/found";
        }
        //  기존 게시글 정보 조회
        //System.out.println("수정화면진입");
        FoundVO foundVO = foundService.foundgetdetail(atcId);
        //System.out.println(foundVO.getFileList());
        //작성자 본인 확인 방어코드
        String loginid=authentication.getName();
        
        //  관리자 권한 여부 확인
        boolean isAdmin=authentication.getAuthorities().stream()
            .anyMatch(auth->auth.getAuthority().equals("ROLE_ADMIN")||
            auth.getAuthority().equals("ADMIN"));

            // 4. 권한 방어 코드: 관리자가 아니면서, 글 작성자 ID와 현재 로그인한 유저 ID가 다르면 수정 불가
        if(!isAdmin && (foundVO.getId()==null||!foundVO.getId().equals(loginid))){
            redirectAttributes.addFlashAttribute("errorMessage", "본인이 작성한 글만 수정할 수 있습니다");
            return "redirect:/api/found/detail/"+atcId;    
        }
        //  수정 화면으로 기존 데이터 및 설정값 전달 (작성 폼 화면인 'found/write'를 재사용)
        
        model.addAttribute("foundVO", foundVO);
        model.addAttribute("writetype", "found");
        model.addAttribute("isEdit", true); // 폼 화면에서 '수정 모드' 임을 구분하기 위한 태그
        
        return "found/write";
    }
    



    // 실제 데이터 수정처리
    @PostMapping("/api/found/update")
    public String FoundUpdate(@Valid @ModelAttribute("foundVO")FoundVO foundVO,
    BindingResult bindingResult,
    RedirectAttributes redirectAttributes,Authentication authentication,Model model,
    @RequestParam(value = "files",required = false)MultipartFile[] files,
    @RequestParam(value = "deleteFiles",required = false )List<String>deleteFiles) {
    
    // 로그인 여부체크
    if(authentication==null){
    redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다");
        return "redirect:/api/found";
    }

    String atcId=foundVO.getAtcId();

    if(atcId==null ||atcId.isEmpty()){
        redirectAttributes.addFlashAttribute("errorMessage", "잘못된 접근입니다");
        return "redirect:/api/found";
    }

    // DB에서 원본 데이터 조회하여 대조
    FoundVO originVO=foundService.foundgetdetail(atcId);
    String loginid=authentication.getName();
    //  관리자 권한 여부 확인
    boolean isAdmin=authentication.getAuthorities().stream()
            .anyMatch(auth->auth.getAuthority().equals("ROLE_ADMIN")||
            auth.getAuthority().equals("ADMIN"));

    //  권한 방어 코드: 관리자가 아니면서, 글 작성자 ID와 현재 로그인한 유저 ID가 다르면 수정 불가
    if(!isAdmin){
        if(originVO.getId()==null||!originVO.getId().equals(loginid)){
            redirectAttributes.addFlashAttribute("errorMessage", "본인이 작성자한 글만 수정 할 수 있습니다");
            return "redirect:/api/found/detail/"+atcId;
        }
    }
    //[입력값 유지 및 임시저장 기능] 검증 에러 발생 시 작성하던 내용 그대로 다시 폼으로 백! 
    if(bindingResult.hasErrors()){
        model.addAttribute("writetype", "found");
        model.addAttribute("isEdit", true);
        // 이미 매핑된 foundVO가 model에 담겨 있으므로 입력했던 값들이 폼에 그대로 유지
        return "found/write";
    }
        //  비즈니스 로직 수행 (정보 수정 및 새 파일 업로드, 구 파일 삭제)
        foundService.UpdateFound(foundVO,files,deleteFiles);
        //  수정이 완료되면 다시 해당 글의 상세 페이지로 이동
        return "redirect:/api/found/detail/"+atcId;
    }

    
    
}
