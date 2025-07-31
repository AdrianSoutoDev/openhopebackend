package es.udc.OpenHope.utils;

import jakarta.servlet.http.HttpServletRequest;

public class HeaderUtils {
  public static String getClientIp(HttpServletRequest request) {
    String ipAddress = request.getHeader("X-Forwarded-For");
    if (ipAddress == null || ipAddress.isEmpty()) {
      ipAddress = request.getRemoteAddr();
    }
    return ipAddress;
  }
}
