package com.kidaristudio.vacationcouponlottery.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 웹 페이지 컨트롤러
 * 사용자 인터페이스 페이지를 제공합니다.
 */
@Hidden // Swagger 문서에서 제외
@Controller
public class WebController {

    /**
     * 응모 페이지
     * 코인 획득, 응모 등록/취소, 현황 조회 기능을 제공합니다.
     */
    @GetMapping("/enter")
    public String enterPage() {
        return "enter";
    }

    /**
     * 추첨 페이지 (관리자용)
     * 추첨 실행 인터페이스를 제공합니다.
     */
    @GetMapping("/draw")
    public String drawPage() {
        return "draw";
    }

    /**
     * 당첨자 조회 페이지
     * 당첨자 목록을 표시합니다.
     */
    @GetMapping("/winner")
    public String winnerPage() {
        return "winner";
    }

    /**
     * 메인 페이지 (응모 페이지로 리다이렉트)
     */
    @GetMapping("/")
    public String mainPage() {
        return "redirect:/enter";
    }
}