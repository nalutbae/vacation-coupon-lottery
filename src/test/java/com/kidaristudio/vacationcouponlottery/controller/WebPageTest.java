package com.kidaristudio.vacationcouponlottery.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 웹 페이지 존재 테스트
 * 각 웹 페이지 URL의 접근 가능성을 확인합니다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class WebPageTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("메인 페이지 접근 테스트 - 응모 페이지로 리다이렉트")
    void mainPage_RedirectsToEnterPage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/enter"));
    }

    @Test
    @DisplayName("응모 페이지 접근 테스트")
    void enterPage_AccessibleAndReturnsHtml() throws Exception {
        mockMvc.perform(get("/enter"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("휴가 쿠폰 추첨 시스템")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("응모 코인 관리")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("휴가 쿠폰 응모")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("내 응모 현황")));
    }

    @Test
    @DisplayName("추첨 페이지 접근 테스트")
    void drawPage_AccessibleAndReturnsHtml() throws Exception {
        mockMvc.perform(get("/draw"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("휴가 쿠폰 추첨 관리")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("관리자 전용 페이지")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("선택적 추첨 실행")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("일괄 추첨 실행")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("당첨자 목록")));
    }

    @Test
    @DisplayName("당첨자 조회 페이지 접근 테스트")
    void winnerPage_AccessibleAndReturnsHtml() throws Exception {
        mockMvc.perform(get("/winner"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("휴가 쿠폰 당첨자 조회")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("총 당첨자")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("쿠폰 종류 필터")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("전화번호 검색")));
    }

    @Test
    @DisplayName("존재하지 않는 페이지 접근 테스트")
    void nonExistentPage_Returns404() throws Exception {
        mockMvc.perform(get("/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("웹 페이지 네비게이션 링크 테스트")
    void webPages_ContainNavigationLinks() throws Exception {
        // 응모 페이지에서 다른 페이지로의 링크 확인
        mockMvc.perform(get("/enter"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("href=\"/draw\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("href=\"/winner\"")));

        // 추첨 페이지에서 다른 페이지로의 링크 확인
        mockMvc.perform(get("/draw"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("href=\"/enter\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("href=\"/winner\"")));

        // 당첨자 조회 페이지에서 다른 페이지로의 링크 확인
        mockMvc.perform(get("/winner"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("href=\"/enter\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("href=\"/draw\"")));
    }

    @Test
    @DisplayName("웹 페이지 Bootstrap CSS 포함 테스트")
    void webPages_IncludeBootstrapCss() throws Exception {
        // 모든 페이지에서 Bootstrap CSS 포함 확인
        String[] pages = {"/enter", "/draw", "/winner"};
        
        for (String page : pages) {
            mockMvc.perform(get(page))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("bootstrap@5.3.0")));
        }
    }

    @Test
    @DisplayName("웹 페이지 JavaScript 포함 테스트")
    void webPages_IncludeJavaScript() throws Exception {
        // 모든 페이지에서 JavaScript 포함 확인
        String[] pages = {"/enter", "/draw", "/winner"};
        
        for (String page : pages) {
            mockMvc.perform(get(page))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("<script>")))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("bootstrap.bundle.min.js")));
        }
    }

    @Test
    @DisplayName("웹 페이지 반응형 메타 태그 테스트")
    void webPages_IncludeResponsiveMetaTags() throws Exception {
        // 모든 페이지에서 반응형 메타 태그 확인
        String[] pages = {"/enter", "/draw", "/winner"};
        
        for (String page : pages) {
            mockMvc.perform(get(page))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("viewport")))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("width=device-width")));
        }
    }

    @Test
    @DisplayName("웹 페이지 한글 인코딩 테스트")
    void webPages_SupportKoreanEncoding() throws Exception {
        // 모든 페이지에서 UTF-8 인코딩 및 한글 지원 확인
        String[] pages = {"/enter", "/draw", "/winner"};
        
        for (String page : pages) {
            mockMvc.perform(get(page))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith("text/html;charset=UTF-8"))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("lang=\"ko\"")))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("휴가")));
        }
    }
}