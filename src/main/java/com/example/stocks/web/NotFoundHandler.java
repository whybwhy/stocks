package com.example.stocks.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.ModelAndView;

/**
 * 매핑되지 않은 요청(NoHandlerFoundException) 시 404 페이지를 반환합니다.
 * application.yml의 spring.mvc.throw-exception-if-no-handler-found: true 필요.
 */
@ControllerAdvice
public class NotFoundHandler {

    @ExceptionHandler(NoHandlerFoundException.class)
    public ModelAndView handleNoHandlerFound(NoHandlerFoundException ex) {
        ModelAndView mav = new ModelAndView("error/404");
        mav.setStatus(HttpStatus.NOT_FOUND);
        return mav;
    }
}
