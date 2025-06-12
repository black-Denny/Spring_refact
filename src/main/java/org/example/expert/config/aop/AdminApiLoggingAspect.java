package org.example.expert.config.aop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
/*

용어	설명
- Aspect     공통 기능(예: 로깅, 트랜잭션 등)을 정의한 클래스
- Join Point 실행 지점 (예: 메서드 실행 시점)
- Advice	 실제로 수행될 공통 로직 (Before, After, Around 등)
- Pointcut	 공통 기능을 어디에 적용할지 정하는 표현식

 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AdminApiLoggingAspect {

    private final ObjectMapper objectMapper;

    @Pointcut("@annotation(AdminApiLogging)")
    public void adminApiAnnotation() {}

    @Pointcut("execution(* org.example.expert.domain..controller.*AdminController.*(..))")
    public void adminController() {}

    @Around("adminApiAnnotation() || adminController()")
    public Object logAdminApi(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return joinPoint.proceed();
        }

        Long userId = (Long) request.getAttribute("userId");
        String url = request.getRequestURI();
        String method = request.getMethod();
        Object requestBody = joinPoint.getArgs().length > 0 ? joinPoint.getArgs()[0] : null;

        log.info("[Admin API] Request - time={}, userId={}, method={}, url={}, body={}",
                LocalDateTime.now(),
                userId,
                method,
                url,
                serializeSafely(requestBody)
        );

        Object result = joinPoint.proceed();

        log.info("[Admin API] Response - time={}, userId={}, url={}, body={}",
                LocalDateTime.now(),
                userId,
                url,
                serializeSafely(result)
        );

        return result;
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return (attrs != null) ? attrs.getRequest() : null;
    }

    private String serializeSafely(Object obj) {
        if (obj == null) return "null";
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize object: {}", e.getMessage());
            return "serialization_error";
        }
    }
}
