package com.campus.lostfound.filter;

import com.campus.lostfound.model.User;
import com.campus.lostfound.util.JsonUtil;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Authentication and Authorization Servlet Filter.
 * Enforces UTF-8 character encoding and secures administrative endpoints.
 */
@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // Force UTF-8 Encoding
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // Prevent browser from caching dynamic web app resources
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);

        // HTTP Security Headers (Defense-in-Depth)
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("X-Frame-Options", "SAMEORIGIN");
        resp.setHeader("X-XSS-Protection", "1; mode=block");
        resp.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        String path = req.getRequestURI();

        // Check if accessing admin-restricted routes
        if (path.contains("/api/admin")) {
            HttpSession session = req.getSession(false);
            User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

            if (currentUser == null) {
                JsonUtil.sendErrorResponse(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required to access admin desk.");
                return;
            }

            if (!"ADMIN".equalsIgnoreCase(currentUser.getRole()) && !"STAFF".equalsIgnoreCase(currentUser.getRole())) {
                JsonUtil.sendErrorResponse(resp, HttpServletResponse.SC_FORBIDDEN, "Forbidden: Administrative role required.");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}
