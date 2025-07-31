package es.udc.OpenHope.dto.mappers;

import es.udc.OpenHope.dto.UserDto;
import es.udc.OpenHope.model.User;

public abstract class UserMapper {
    public static UserDto toUserDto(User user){
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        return userDto;
    }
}
