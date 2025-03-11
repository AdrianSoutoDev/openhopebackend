package es.udc.OpenHope.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class UserServiceTest {

    private final UserService userService;

    @Autowired
    public UserServiceTest(final UserService userService) {
        this.userService = userService;
    }
}
