package org.killercarrots.evcharge.auth;

public class SecurityConstants {
  // Set to static for global using constant values
  // (else could be configured in application.properties)
  public static final String SECRET = "SECRET_KEY";
  public static final String HEADER_STRING = "X-OBSERVATORY-AUTH";
  public static final String HEADER_PREFIX = "Bearer ";
}
