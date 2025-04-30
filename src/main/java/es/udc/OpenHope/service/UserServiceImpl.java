package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.UserDto;
import es.udc.OpenHope.dto.mappers.UserMapper;
import es.udc.OpenHope.exception.DuplicateEmailException;
import es.udc.OpenHope.model.User;
import es.udc.OpenHope.repository.AccountRepository;
import es.udc.OpenHope.repository.UserRepository;
import es.udc.OpenHope.utils.Messages;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends AccountServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder,
                           AccountRepository accountRepository) {
        super(bCryptPasswordEncoder, accountRepository);
        this.userRepository = userRepository;
    }

    @Override
    public UserDto create(String email, String password) throws DuplicateEmailException {
        if(email == null) throw new IllegalArgumentException(  Messages.get("validation.email.null") );
        if(password == null) throw new IllegalArgumentException( Messages.get("validation.password.null") );

        if(accountExists(email)) {
            throw new DuplicateEmailException( Messages.get("validation.email.duplicated") );
        }

        String encryptedPassword = bCryptPasswordEncoder.encode(password);
        User user = new User(email, encryptedPassword);

        userRepository.save(user);
        return UserMapper.toUserDto(user);
    }
}
