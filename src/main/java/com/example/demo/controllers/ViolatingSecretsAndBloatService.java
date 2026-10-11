package com.example.demo.controllers;

import org.springframework.stereotype.Service;

/**
 * ❌ AG013: Hardcoded Secret / Password / Token
 * ❌ AG017: Injected Dependency Bloat (>= 7 dependencies in single bean)
 */
@Service
public class ViolatingSecretsAndBloatService {

    // ❌ AG013: Hardcoded Secrets Scanner
    private String apiKey = "custom_production_api_key_xyz123456789";
    private String jwtSecret = "superSecretJwtSigningKey9876543210!#$";

    // ❌ AG017: Injected Dependency Bloat (7 injected dependencies violating Single Responsibility)
    private final Dep1 dep1;
    private final Dep2 dep2;
    private final Dep3 dep3;
    private final Dep4 dep4;
    private final Dep5 dep5;
    private final Dep6 dep6;
    private final Dep7 dep7;

    public ViolatingSecretsAndBloatService(Dep1 dep1, Dep2 dep2, Dep3 dep3, Dep4 dep4, Dep5 dep5, Dep6 dep6, Dep7 dep7) {
        this.dep1 = dep1;
        this.dep2 = dep2;
        this.dep3 = dep3;
        this.dep4 = dep4;
        this.dep5 = dep5;
        this.dep6 = dep6;
        this.dep7 = dep7;
    }

    public static class Dep1 {}
    public static class Dep2 {}
    public static class Dep3 {}
    public static class Dep4 {}
    public static class Dep5 {}
    public static class Dep6 {}
    public static class Dep7 {}
}
