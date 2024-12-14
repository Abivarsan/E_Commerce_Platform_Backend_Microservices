package com.ecommerce.user_service.jwt;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ecommerce.user_service.dto.LoginInfo;
import com.ecommerce.user_service.dto.UserDTO;
import com.ecommerce.user_service.service.UserService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Service
public class JwtUtil {
    private final UserService userService;
    @Autowired
    public JwtUtil(UserService userService){this.userService=userService;}

    @Value("${jwt.secret}")
    private String secret;

    private Key key;

    @PostConstruct
    public void init(){
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(LoginInfo loginInfo) {
        Optional<UserDTO> usr = userService.getUser(loginInfo.getUserName());
        Map<String, Object> claims = new HashMap<>();
        claims.put("userName",usr.get().getUserName());
        claims.put("role","CUSTOMER");
        return createToken(claims);
    }
    private String createToken(Map<String, Object> claims) {
        return Jwts.builder().setClaims(claims).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // token will expire after 10 hours
                .signWith(SignatureAlgorithm.HS256, key).compact();
    }
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
}
