package org.example.expert.config.interceptor;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminApiLoggingInterceptorTest {

    private AdminApiLoggingInterceptor interceptor;
    private MockHttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        interceptor = new AdminApiLoggingInterceptor();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/admin/test");
    }

    @Test
    @DisplayName("userRole이 없으면 401 응답 후 false 반환")
    void testPreHandle_userRoleMissing() throws Exception {
        // when
        boolean result = interceptor.preHandle(request, response, new Object());

        // then
        assertFalse(result);
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "User role missing");
    }

    @Test
    @DisplayName("userRole이 ADMIN이 아니면 401 응답 후 false 반환")
    void testPreHandle_userRoleIsNotAdmin() throws Exception {
        // given
        request.setAttribute("userRole", "USER");

        // when
        boolean result = interceptor.preHandle(request, response, new Object());

        // then
        assertFalse(result);
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Admin access required");
    }

    @Test
    @DisplayName("userRole이 ADMIN이면 true 반환")
    void testPreHandle_userRoleIsAdmin() throws Exception {
        // given
        request.setAttribute("userRole", "ADMIN");

        // when
        boolean result = interceptor.preHandle(request, response, new Object());

        // then
        assertTrue(result);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    @DisplayName("userRole이 admin (소문자)여도 허용됨")
    void testPreHandle_userRoleLowercaseAdmin() throws Exception {
        // given
        request.setAttribute("userRole", "admin");

        // when
        boolean result = interceptor.preHandle(request, response, new Object());

        // then
        assertTrue(result);
        verify(response, never()).sendError(anyInt(), anyString());
    }
}
