package es.udc.OpenHope.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.List;

public class CookieUtils {

  public static Cookie getCookie(String name, String value, Integer expires){
    Cookie cookie = new Cookie(name, value);
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    cookie.setMaxAge(expires);
    cookie.setPath("/");
    return cookie;
  }

  public static Cookie getCookieFromRequest(String name, HttpServletRequest request) {
    Cookie result = null;
    Cookie[] cookies = request.getCookies();

    if (cookies != null) {
      List<Cookie> cookieList  = Arrays.asList(cookies);
      result = cookieList.stream()
          .filter(cookie -> name.equals(cookie.getName()))
          .findFirst().orElse(null);
    }

    return result;
  }

  public static void reNewCookies(String token, String refresh, Integer expires, String aspsp, HttpServletResponse response) {
    Cookie tokenCookie = CookieUtils.getCookie("token_".concat(aspsp),token, expires);
    Cookie refreshCookie = CookieUtils.getCookie("refresh_".concat(aspsp), refresh, expires);
    response.addCookie(tokenCookie);
    response.addCookie(refreshCookie);
  }
}
