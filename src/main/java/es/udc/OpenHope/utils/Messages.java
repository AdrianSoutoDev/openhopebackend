package es.udc.OpenHope.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class Messages {

  private static MessageSource messageSource;

  @Autowired
  public Messages(MessageSource messageSource) {
    Messages.messageSource = messageSource;
  }

  public static String get(String key){
    return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
  }
}
