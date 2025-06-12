package org.example.expert.config.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminApiLoggingAspectTest {

    private AdminApiLoggingAspect aspect;
    private MockHttpServletRequest mockRequest;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @BeforeEach
    void setUp() {
        aspect = new AdminApiLoggingAspect(new ObjectMapper());

        mockRequest = new MockHttpServletRequest();
        mockRequest.setRequestURI("/api/admin/test");
        mockRequest.setMethod("POST");
        mockRequest.setAttribute("userId", 42L);

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));
    }

    @Test
    @DisplayName("정상적인 어드민 API 요청/응답 로깅")
    void testLogAdminApi_normal() throws Throwable {
        // given
        when(joinPoint.getArgs()).thenReturn(new Object[]{"test-body"});
        when(joinPoint.proceed()).thenReturn("response-data");

        // when
        Object result = aspect.logAdminApi(joinPoint);

        // then
        assertEquals("response-data", result);
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    @DisplayName("요청 본문이 없는 경우")
    void testLogAdminApi_noRequestBody() throws Throwable {
        // given
        when(joinPoint.getArgs()).thenReturn(new Object[]{});
        when(joinPoint.proceed()).thenReturn("ok");

        // when
        Object result = aspect.logAdminApi(joinPoint);

        // then
        assertEquals("ok", result);
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    @DisplayName("예외 발생 시 예외 전달")
    void testLogAdminApi_throwsException() throws Throwable {
        // given
        when(joinPoint.getArgs()).thenReturn(new Object[]{"boom!"});
        when(joinPoint.proceed()).thenThrow(new IllegalStateException("mock exception"));

        // when & then
        assertThrows(IllegalStateException.class, () -> aspect.logAdminApi(joinPoint));
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    @DisplayName("RequestContextHolder가 null인 경우")
    void testLogAdminApi_requestContextIsNull() throws Throwable {
        // given
        RequestContextHolder.resetRequestAttributes();
        when(joinPoint.proceed()).thenReturn("ok");

        // when
        Object result = aspect.logAdminApi(joinPoint);

        // then
        assertEquals("ok", result);
        verify(joinPoint, times(1)).proceed();
    }
}
