package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.OrganizationDto;
import es.udc.OpenHope.dto.UserDto;
import es.udc.OpenHope.exception.DuplicateEmailException;
import es.udc.OpenHope.model.Organization;
import es.udc.OpenHope.model.User;
import es.udc.OpenHope.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class UserServiceTest {

    private static final String USER_EMAIL = "user@openhope.com";
    private static final String PASSWORD = "12345abc?";

    private final UserService userService;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public UserServiceTest(final UserService userService, final UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Test
    public void createUserTest() throws DuplicateEmailException {
        UserDto userDto = userService.create(USER_EMAIL, PASSWORD);
        Optional<User> userFinded = userRepository.findById(userDto.getId());
        assertTrue(userFinded.isPresent());
        assertEquals(USER_EMAIL, userFinded.get().getEmail());
    }

    @Test
    public void createUserWithEncryptedPasswordTest() throws DuplicateEmailException {
        UserDto userDto = userService.create(USER_EMAIL, PASSWORD);
        Optional<User> userFinded = userRepository.findById(userDto.getId());
        assertTrue(userFinded.isPresent());
        boolean passwordsAreEquals = bCryptPasswordEncoder.matches(PASSWORD, userFinded.get().getEncryptedPassword());
        assertTrue(passwordsAreEquals);
    }

    @Test
    public void createUserDuplicatedEmailTest() throws DuplicateEmailException {
        UserDto userDto = userService.create(USER_EMAIL, PASSWORD);
        assertThrows(DuplicateEmailException.class, () ->
                userService.create(USER_EMAIL, "anotherPassword"));
    }

    @Test
    public void createOrganizationDuplicatedEmailIgnoringCaseTest() throws DuplicateEmailException {
        UserDto userDto = userService.create("user@openhope.com", PASSWORD);

        assertThrows(DuplicateEmailException.class, () ->
                userService.create("USER@OpenHope.com", "anotherPassword"));
    }

    @Test
    public void createUsersWithDiferentEmailTest() throws DuplicateEmailException {
        UserDto firstUserDto = userService.create(USER_EMAIL, PASSWORD);
        UserDto secondUserDto = userService.create("second_email@openHope.com", PASSWORD);

        List<User> users = userRepository.findAll();
        assertEquals(2, users.size());
    }

    @Test
    public void createUserWithEmailNullTest() {
        assertThrows(IllegalArgumentException.class, () ->
                userService.create(null, PASSWORD));
    }

    @Test
    public void createUserWithPasswordNullTest() throws DuplicateEmailException {
        assertThrows(IllegalArgumentException.class, () ->
                userService.create(USER_EMAIL, null));
    }
}
