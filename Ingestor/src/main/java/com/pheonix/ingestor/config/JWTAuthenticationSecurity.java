package com.pheonix.ingestor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
@EnableWebSecurity
public class JWTAuthenticationSecurity {

    private static final String PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----\n" +
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2gKPaaXvXL86ZT7eWgAd\n" +
            "dMh3YilMXALnF+jzXZHb9f6sUwNALExWHfy1ILmXIhLH0w91lMcA+VXpg4yLjiCY\n" +
            "opie4N3jV2zUCREw7gBjvTKW9e8vu2BCb4EyWxI/flBu1a+gL0fCoQGo49n96yMV\n" +
            "mT1qJ5XPUfEt9jXB53brrttsxW2mh/rdBX5ij43T+fGFfu+NXf3htG1qa25y0b21\n" +
            "hM3KOegdTP1ZbyG+3gnG/A6daN4XAz2MpX4w82zFgXXRavAX0Qn+1kKPLf84Kzhi\n" +
            "L0EQB+cHRt/wlp82uxf5ZTEnw/wLSRc/mrOZbeYYRw1gmSZkZ/6WPXUMJGmDoWaz\n" +
            "fwIDAQAB\n" +
            "-----END PUBLIC KEY-----";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(requestMatchers -> requestMatchers.requestMatchers("/h2-console/**").permitAll());
        httpSecurity.authorizeHttpRequests(request -> request.anyRequest().authenticated());
        httpSecurity.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        httpSecurity.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        httpSecurity.headers(AbstractHttpConfigurer::disable);
        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        return httpSecurity.build();
    }

    private RSAPublicKey getPublicKey() throws Exception {
        String publicKeyPEM = PUBLIC_KEY
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }

    @Bean
    public JwtDecoder jwtDecoder() throws Exception {
        return NimbusJwtDecoder.withPublicKey(getPublicKey()).build();
    }
}