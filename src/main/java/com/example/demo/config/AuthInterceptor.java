package com.example.demo.config;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.example.demo.config.JwtUtil;
import com.example.demo.config.TokenBlacklist;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenBlacklist tokenBlacklist;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        
        // Skip public paths and game/socket endpoints
        if (path.equals("/") || path.equals("/home") || path.equals("/login") || 
            path.equals("/register") || path.equals("/about") || path.equals("/support") ||
            path.equals("/games") || path.startsWith("/games/") || 
            path.startsWith("/api/") || path.startsWith("/ws") ||
            path.startsWith("/css/") || 
            path.startsWith("/js/") || path.startsWith("/images/") || path.startsWith("/uploads/")) {
            return true;
        }

        String token = null;

        // 1. Check Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        // 2. Check Cookie (for traditional links/SSR)
        if (token == null && request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwtToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // 3. Check Query Parameter (for multi-tab support)
        String queryToken = request.getParameter("auth");
        if (queryToken != null) {
            token = queryToken;
        }

        if (token != null) {
            // ── Reject blacklisted (logged-out) tokens immediately ──
            if (tokenBlacklist.isBlacklisted(token)) {
                if (isAjaxRequest(request)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                } else {
                    response.sendRedirect("/login?expired=true");
                }
                return false;
            }

            request.setAttribute("urlToken", token); // Store for postHandle
            try {
                String username = jwtUtil.extractUsername(token);
                if (username != null) {
                    if ("admin".equals(username)) {
                        request.setAttribute("authenticatedUser", "admin");
                        // Prevent browser from caching protected pages
                        setNoCacheHeaders(response);
                        return true;
                    }
                    User user = userRepository.findByUsername(username);
                    if (user != null && jwtUtil.validateToken(token, username)) {
                        // Store user in request for controllers to use
                        request.setAttribute("authenticatedUser", user);
                        // Prevent browser from caching protected pages
                        setNoCacheHeaders(response);
                        return true;
                    }
                }
            } catch (Exception e) {
                // Token invalid or expired
            }
        }

        // Not authenticated
        if (isAjaxRequest(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        response.sendRedirect("/login?error=timeout");
        return false;
    }

    /**
     * Determines if a request is an AJAX or API request.
     */
    private boolean isAjaxRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        String requestedWith = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equals(requestedWith) || 
               path.startsWith("/api/") || 
               path.endsWith("/ajax");
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, org.springframework.web.servlet.ModelAndView modelAndView) throws Exception {
        if (modelAndView != null && modelAndView.getViewName() != null && modelAndView.getViewName().startsWith("redirect:")) {
            String token = (String) request.getAttribute("urlToken");
            if (token != null) {
                String viewName = modelAndView.getViewName();
                if (!viewName.contains("auth=")) {
                    String separator = viewName.contains("?") ? "&" : "?";
                    modelAndView.setViewName(viewName + separator + "auth=" + token);
                }
            }
        }
    }

    /** Set HTTP headers that prevent the browser from caching protected pages.
     *  After logout, hitting Back-button will trigger a fresh request
     *  (which the blacklist check will immediately reject). */
    private void setNoCacheHeaders(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }
}

