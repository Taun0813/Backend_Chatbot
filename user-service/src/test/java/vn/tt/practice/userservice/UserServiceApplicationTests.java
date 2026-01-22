package vn.tt.practice.userservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestContainersConfig.class)
class UserServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
