package com.human.found;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit; // 이 import 추가

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootTest
public class DbInsertTest {

    @Autowired
    private TestMapper testMapper;
    private static final Logger log = LoggerFactory.getLogger(DbInsertTest.class); // 로거 추가

    @Test
    @Commit
    public void 검증용_데이터_인서트_테스트() {
        log.info("★ 테스트 시작!"); // System.out 대신 log.info 사용
        testMapper.insertTestData("데이터 확인");
        log.info("🎉 삽입 성공!");
    }

    // 서비스, 매퍼 파일 분리 없이 한곳에서 테스트하기 위한 내부 인라인 매퍼 인터페이스
    @Mapper
    interface TestMapper {
        // [주의] 실제 사용 중인 테이블명(예: test_table)과 컬럼명(예: content)으로 수정해야 작동합니다.
        @Insert("INSERT INTO test (a) VALUES (#{e})")
        void insertTestData(@Param("e") String content);
    }
}