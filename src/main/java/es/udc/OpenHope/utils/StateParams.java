package es.udc.OpenHope.utils;

import java.util.HashMap;

public class StateParams {

  public static HashMap<String, String> getStateParams(String state) {
    HashMap<String, String> result = new HashMap<>();
    String[] params = state.split(",");

    for (String param : params) {
      String[] keyValue = param.split("=");
      if (keyValue.length == 2) {
        result.put(keyValue[0], keyValue[1]);
      }
    }

    return result;
  }
}
