package com.walmart.realestate.crystal.storereview.service;

import com.walmart.core.realestate.cerberus.bean.CerberusUserInformation;
import com.walmart.realestate.soteria.model.UserContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.Collections;
import java.util.Objects;

public class AuthenticationHelper {

    private Authentication originalAuthentication;

    public AuthenticationHelper(String username, Boolean overrideContext) {
        if (Objects.isNull(SecurityContextHolder.getContext().getAuthentication()) || overrideContext) {
            CerberusUserInformation userInformation = new CerberusUserInformation();
            userInformation.setUserName(username);
            UserContext userContext = new UserContext(userInformation, Collections.singleton("default"));
            Authentication authentication = new PreAuthenticatedAuthenticationToken(userContext, "", userContext.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            originalAuthentication = SecurityContextHolder.getContext().getAuthentication();
        }
    }

    public void resetAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(originalAuthentication);
    }

}