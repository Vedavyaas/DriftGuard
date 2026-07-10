package com.pheonix.authentication.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.pheonix.authentication.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
@EnableWebSecurity
public class JWTAuthenticationConfig {

    private static final String PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----\n" +
            "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDaAo9ppe9cvzpl\n" +
            "Pt5aAB10yHdiKUxcAucX6PNdkdv1/qxTA0AsTFYd/LUguZciEsfTD3WUxwD5VemD\n" +
            "jIuOIJiimJ7g3eNXbNQJETDuAGO9Mpb17y+7YEJvgTJbEj9+UG7Vr6AvR8KhAajj\n" +
            "2f3rIxWZPWonlc9R8S32NcHnduuu22zFbaaH+t0FfmKPjdP58YV+741d/eG0bWpr\n" +
            "bnLRvbWEzco56B1M/VlvIb7eCcb8Dp1o3hcDPYylfjDzbMWBddFq8BfRCf7WQo8t\n" +
            "/zgrOGIvQRAH5wdG3/CWnza7F/llMSfD/AtJFz+as5lt5hhHDWCZJmRn/pY9dQwk\n" +
            "aYOhZrN/AgMBAAECggEAAoWBDyHCMPptPHqLq9LQhGdFKjd8EJxf++DP38cuGhxF\n" +
            "9ffVqC2B2XrwxV4gJ168PO6y13OtyTR7LPBVNOGfolHNlKA8Xuh9U1WFkVsjzC5n\n" +
            "nJSVor8sRYnjATam0VkwzvvnCGT2FVKll20QCMYxG8KgFbEf4ry9YVmh0oGHnFAb\n" +
            "6yQDJxQfdmfU8eecAfDTE/ABhXK5yepl/t4JrfbSKTIh1QT9JHdZXCsBxFNxRguy\n" +
            "2K1APfni0jn3byCX/RughaTX27TVjTiVnuI78VCHqls98/qGQHRep+233+/G0VH5\n" +
            "32m8K3r4E4vPTVWnrgq6fpLmt4gMIBV/KUCObyKRgQKBgQD4lwzUY9Wl5ILP0A06\n" +
            "WSbIAyMNodBMBmFltKtkVrV0lPCEJO2I0pay1slTdJ1dhjVHHMvc+DF9bnfp2bIp\n" +
            "Udq+a7td3MCYKattNpyvBeVz5QNhiWgWrtpZ79J7hj6oLKTZR27C/jl4aHWHlQTM\n" +
            "IyAqrT9jdlvubtPGSnySn9za4QKBgQDggiiokdCZkVkBnN3X7Cmi4hpcHrZJItav\n" +
            "2ozVvbviXd2aIC82GPfvCFkd9YLWYe6mjOH1aDex9pbw8orza9oEYgmqO6bdiWue\n" +
            "blCx0274H2FgZ15wmdabR27olDTs4dIm1zI8Ziz09CNws2gMEubzSVH3V185qZty\n" +
            "2rJIPqq6XwKBgDA8t0ubV8DKF6wVlguFcyYKncmuZYnrDwk6RqvAu5M0t3sc407S\n" +
            "dlWbohNkpIiaW0pCRzjInGnXfsNM7+perNCYfRIYnPKp207k8wBvZ3fWr5JEpIMK\n" +
            "SOp8w94eTZg6mX5kAxUpWaOIuY2ml/i4tCwz1AIL3IjmzodCuuuEw8lhAoGBAK8P\n" +
            "DuESghOcsEh/psQdFAI8R82Y7Q5uKfUJd1ObFIvYIVlyyx1lrr4mRkcEDzdXOZ2I\n" +
            "sRCIaRVNcDkFBq/0YxWT1HF2/hA0fUQWRLnEYfz7ixR+xbcLXbnKo4KbPgGPvCwp\n" +
            "FLTf/yIp1BCm8/SmMIt3moBBErGFvP575RRMORgFAoGATOSpOl71DCvC686zipHU\n" +
            "kqtHGS2oveoDYpXkFzsbadcQSDdUT98vTbOT4+4PRL6hvLwZ/lujlHFFL85qnyKm\n" +
            "FayHTIvteBYa/A2O/HIpiRrAQsdBNdRBMDBM1zH55BSPulFiNrvMvRLiFcdoaS87\n" +
            "qmVGRdnmaRwrI/Yezu1FdUg=\n" +
            "-----END PRIVATE KEY-----";

    private static final String PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----\n" +
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2gKPaaXvXL86ZT7eWgAd\n" +
            "dMh3YilMXALnF+jzXZHb9f6sUwNALExWHfy1ILmXIhLH0w91lMcA+VXpg4yLjiCY\n" +
            "opie4N3jV2zUCREw7gBjvTKW9e8vu2BCb4EyWxI/flBu1a+gL0fCoQGo49n96yMV\n" +
            "mT1qJ5XPUfEt9jXB53brrttsxW2mh/rdBX5ij43T+fGFfu+NXf3htG1qa25y0b21\n" +
            "hM3KOegdTP1ZbyG+3gnG/A6daN4XAz2MpX4w82zFgXXRavAX0Qn+1kKPLf84Kzhi\n" +
            "L0EQB+cHRt/wlp82uxf5ZTEnw/wLSRc/mrOZbeYYRw1gmSZkZ/6WPXUMJGmDoWaz\n" +
            "fwIDAQAB\n" +
            "-----END PUBLIC KEY-----";

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

    private RSAPrivateKey getPrivateKey() throws Exception {
        String privateKeyPEM = PRIVATE_KEY
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }

    @Bean
    public JwtDecoder jwtDecoder() throws Exception {
        return NimbusJwtDecoder.withPublicKey(getPublicKey()).build();
    }

    @Bean
    public JwtEncoder jwtEncoder() throws Exception {
        RSAKey jwk = new RSAKey.Builder(getPublicKey()).privateKey(getPrivateKey()).build();
        JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwks);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(http -> http
                .requestMatchers("/api/user/**").permitAll()
                .anyRequest().authenticated()
        );
        httpSecurity.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        httpSecurity.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        httpSecurity.csrf(AbstractHttpConfigurer::disable);
        httpSecurity.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));

        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(PasswordEncoder passwordEncoder, UserDetailsServiceImpl userDetailsService) {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);

        return daoAuthenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}