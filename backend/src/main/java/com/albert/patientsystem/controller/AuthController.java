package com.albert.patientsystem.controller;

import com.albert.patientsystem.entity.User;
import com.albert.patientsystem.repository.UserRepository;
import com.albert.patientsystem.security.AppUserDetailsService;
import com.albert.patientsystem.security.CurrentUser;
import com.albert.patientsystem.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Login / logout / me / register. Session-based — the browser holds
 * an HttpOnly JSESSIONID cookie after a successful POST to /login.
 *
 * We do the password check here (UserDetailsService + PasswordEncoder)
 * instead of through Spring Security's AuthenticationManager because
 * exposing the manager as a bean while also disabling form login
 * triggers an autoconfiguration conflict in Spring Security 6.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final AppUserDetailsService userDetailsService;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          AuditService auditService,
                          AppUserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
        this.userDetailsService = userDetailsService;
    }

    /** GET /api/auth/me — current user, or 401. */
    @GetMapping("/me")
    public ResponseEntity<?> me() {
        String username = CurrentUser.username();
        if (username == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        User u = user.get();
        return ResponseEntity.ok(Map.of(
            "username", u.getUsername(),
            "role", u.getRole(),
            "displayName", u.getDisplayName() == null ? u.getUsername() : u.getDisplayName()
        ));
    }

    /** POST /api/auth/login — form-encoded or JSON {username, password}. */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam(value = "username", required = false) String formUsername,
                                   @RequestParam(value = "password", required = false) String formPassword,
                                   @RequestBody(required = false) Map<String, String> jsonBody,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {
        String username = formUsername != null ? formUsername : (jsonBody != null ? jsonBody.get("username") : null);
        String password = formPassword != null ? formPassword : (jsonBody != null ? jsonBody.get("password") : null);
        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "username and password required"));
        }

        // Manual credential check using the same UserDetailsService Spring
        // Security would have used. Avoids the AuthenticationManager bean
        // conflict described in SecurityConfig.
        try {
            UserDetails details = userDetailsService.loadUserByUsername(username);
            if (!passwordEncoder.matches(password, details.getPassword())) {
                throw new BadCredentials("bad password");
            }
            // Build the Authentication and persist it to the session.
            List<GrantedAuthority> authorities = details.getAuthorities().stream()
                .map(a -> (GrantedAuthority) a)
                .toList();
            Authentication auth = new UsernamePasswordAuthenticationToken(details, null, authorities);
            SecurityContext ctx = SecurityContextHolder.createEmptyContext();
            ctx.setAuthentication(auth);
            SecurityContextHolder.setContext(ctx);
            // Save to HttpSession so subsequent requests carry the JSESSIONID cookie.
            HttpSession session = request.getSession(true);
            securityContextRepository.saveContext(ctx, request, response);

            User u = userRepository.findByUsername(username).orElseThrow();
            auditService.log(username, "LOGIN", null, null, null);
            return ResponseEntity.ok(Map.of(
                "username", u.getUsername(),
                "role", u.getRole(),
                "displayName", u.getDisplayName() == null ? u.getUsername() : u.getDisplayName()
            ));
        } catch (Exception e) {
            auditService.log(username, "LOGIN_FAILED", null, null, Map.of("reason", "bad_credentials"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid username or password"));
        }
    }

    /** POST /api/auth/register — dev-only user provisioning. */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String role = body.getOrDefault("role", "CLINICIAN");
        String displayName = body.get("displayName");
        if (username == null || username.isBlank() || password == null || password.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "username and password (≥6 chars) required"));
        }
        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "username already exists"));
        }
        User u = new User();
        u.setUsername(username);
        u.setPasswordHash(passwordEncoder.encode(password));
        u.setRole(role);
        u.setDisplayName(displayName);
        userRepository.save(u);
        auditService.log(username, "REGISTER_USER", "USER", u.getId(), Map.of("role", role));
        return ResponseEntity.ok(Map.of("username", u.getUsername(), "role", u.getRole()));
    }

    /** POST /api/auth/logout — invalidate session. */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String username = CurrentUser.username();
        try {
            HttpSession session = request.getSession(false);
            if (session != null) session.invalidate();
            SecurityContextHolder.clearContext();
        } catch (Exception ignored) {}
        auditService.log(username, "LOGOUT", null, null, null);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    /** Tiny local exception so the catch block reads naturally. */
    private static class BadCredentials extends RuntimeException {
        BadCredentials(String msg) { super(msg); }
    }
}
