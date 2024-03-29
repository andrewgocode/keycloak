package ru.learning.keycloak.token;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import ru.learning.keycloak.mapper.UserMapper;
import ru.learning.keycloak.model.User;
import ru.learning.keycloak.token.JWSAuthenticationToken;
import ru.learning.keycloak.token.KeycloakTokenVerifier;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
//@Qualifier("websoket")
@AllArgsConstructor
public class KeycloakWebSocketAuthManager implements AuthenticationManager {
    private final KeycloakTokenVerifier tokenVerifier;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException
    {
        JWSAuthenticationToken token = (JWSAuthenticationToken) authentication;
        String tokenString = (String) token.getCredentials();
        try
        {
            AccessToken accessToken = tokenVerifier.verifyToken(tokenString);
            List<GrantedAuthority> authorities = accessToken.getRealmAccess().getRoles().stream()
                    .map(SimpleGrantedAuthority::new).collect(Collectors.toList());
            //Map the token to the user
            UserMapper userMapper = new UserMapper();
            User user = userMapper.accessTokenToUserMapper(accessToken);
            token = new JWSAuthenticationToken(tokenString, user, authorities);
            token.setAuthenticated(true);
        }
        catch (VerificationException e)
        {
            log.debug("Exception authenticating the token {}:", tokenString, e);
            throw new BadCredentialsException("Invalid token");
        }
        return token;
    }
}
