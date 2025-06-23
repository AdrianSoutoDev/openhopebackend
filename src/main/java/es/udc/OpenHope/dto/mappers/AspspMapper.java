package es.udc.OpenHope.dto.mappers;

import es.udc.OpenHope.dto.AspspParamsDto;
import es.udc.OpenHope.model.Aspsp;

public abstract class AspspMapper {
  public static Aspsp toAspsp(AspspParamsDto aspspParamsDto){
    Aspsp aspsp = new Aspsp();
    aspsp.setName(aspspParamsDto.getName());
    aspsp.setCode(aspspParamsDto.getCode());
    aspsp.setProvider(aspspParamsDto.getProvider());
    return aspsp;
  }
}
