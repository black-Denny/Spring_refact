package org.example.expert.config.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;

/*
[요청]
  ↓
[Filter] ← 서블릿 레벨 (전역 처리)
  ↓
[DispatcherServlet]
  ↓
[Interceptor - preHandle()] ← 컨트롤러 전에 개입
  ↓
[Controller]
  ↓
[Interceptor - postHandle()] ←  컨트롤러 실행 후, View 렌더링 전
  ↓
[Interceptor - afterCompletion()] ← 뷰 렌더링 이후 정리 작업
  ↓
[응답]
*/

@Slf4j
public class AdminApiLoggingInterceptor implements HandlerInterceptor {
//    private static final String ADMIN_ROLE = "ADMIN";
//    private static final String ATTRIBUTE_USER_ROLE = "userRole";


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userRole = (String) request.getAttribute("userRole");
        String requestURI = request.getRequestURI();
        LocalDateTime now = LocalDateTime.now();

        if (userRole == null) {
            log.warn("[Admin API] Access denied - Time: {}, URL: {}, Reason: Missing user role", now, requestURI);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User role missing");
            return false;
        }

        if (!"ADMIN".equalsIgnoreCase(userRole)) {
            log.warn("[Admin API] Unauthorized access attempt - Time: {}, URL: {}, Role: {}", now, requestURI, userRole);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Admin access required");
            return false;
        }

        log.info("[Admin API] Admin access granted - Time: {}, URL: {}, Role: {}", now, requestURI, userRole);
        return true;
    }
}
