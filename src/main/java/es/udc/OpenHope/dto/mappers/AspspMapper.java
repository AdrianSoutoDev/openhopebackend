package es.udc.OpenHope.dto.mappers;

import es.udc.OpenHope.dto.AspspDto;
import es.udc.OpenHope.dto.AspspParamsDto;
import es.udc.OpenHope.enums.Provider;
import es.udc.OpenHope.model.Aspsp;

public abstract class AspspMapper {
  public static Aspsp toAspsp(AspspParamsDto aspspParamsDto){
    Aspsp aspsp = new Aspsp();
    aspsp.setName(aspspParamsDto.getName());
    aspsp.setCode(aspspParamsDto.getCode());
    aspsp.setProvider(aspspParamsDto.getProvider());
    return aspsp;
  }

  public static AspspDto toAspspDto(Aspsp aspsp){
    AspspDto aspspDto = new AspspDto();
    aspspDto.setName(aspsp.getName());
    aspspDto.setCode(aspsp.getCode());
    aspspDto.setProvider(Provider.valueOf(aspsp.getProvider()));
    return aspspDto;
  }

  public static AspspDto toAspspDto(AspspParamsDto aspspParamsDto){
    AspspDto aspspDto = new AspspDto();
    aspspDto.setName(aspspParamsDto.getName());
    aspspDto.setCode(aspspParamsDto.getCode());
    aspspDto.setProvider(Provider.valueOf(aspspParamsDto.getProvider()));
    return aspspDto;
  }
}
