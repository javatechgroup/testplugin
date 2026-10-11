package com.example.demo.boundary.external;

import com.example.demo.boundary.internal.InternalSecurityToken;

// ❌ AG006: External package directly couples to prohibited internal package
public class ExternalGatewayService {
    private final InternalSecurityToken tokenService = new InternalSecurityToken();

    public String getProhibitedToken() {
        return InternalSecurityToken.getToken();
    }
}
