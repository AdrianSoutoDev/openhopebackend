package es.udc.OpenHope.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.List;

public class CookieUtils {

  public static Cookie getCookie(String name, String value){
    Cookie cookie = new Cookie(name, value);
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
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
}
