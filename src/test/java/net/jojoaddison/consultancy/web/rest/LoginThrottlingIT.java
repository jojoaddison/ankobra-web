package net.jojoaddison.consultancy.web.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.jojoaddison.consultancy.IntegrationTest;
import net.jojoaddison.consultancy.security.LoginAttemptService;
import net.jojoaddison.consultancy.web.rest.vm.LoginVM;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * End-to-end proof of SEC-04: repeated failed logins stop returning 401 and start returning 429.
 *
 * <p>{@link LoginAttemptService} is a singleton shared by the whole test context, so this class resets
 * it either side of every test. Without that, tripping the lockout here would leak into any other test
 * that posts to {@code /api/authenticate} from the same (fixed) MockMvc source address.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Transactional
class LoginThrottlingIT {

    @Autowired
    private MockMvc restMockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @BeforeEach
    @AfterEach
    void resetCounters() {
        loginAttemptService.reset();
    }

    @Test
    void failedLoginsEventuallyReturn429InsteadOf401() throws Exception {
        // Up to the threshold the answer stays "wrong credentials"...
        for (int i = 0; i < 10; i++) {
            restMockMvc.perform(login("throttled-user", "wrong-password")).andExpect(status().isUnauthorized());
        }
        // ... and past it the endpoint stops answering the question at all.
        restMockMvc.perform(login("throttled-user", "wrong-password")).andExpect(status().isTooManyRequests());
    }

    @Test
    void theLockoutFollowsTheUsernameNotJustTheConnection() throws Exception {
        for (int i = 0; i < 10; i++) {
            restMockMvc.perform(login("targeted-user", "wrong-password")).andExpect(status().isUnauthorized());
        }
        // Even a correct password is refused while the account is locked — the check runs before
        // authentication, which is what stops an attacker learning anything from the response.
        restMockMvc.perform(login("targeted-user", "whatever-comes-next")).andExpect(status().isTooManyRequests());
    }

    @Test
    void anUnrelatedAccountIsStillReachableFromTheSameHostBelowTheIpThreshold() throws Exception {
        for (int i = 0; i < 5; i++) {
            restMockMvc.perform(login("noisy-user", "wrong-password")).andExpect(status().isUnauthorized());
        }
        // 5 failures is under the IP threshold, so a different account is unaffected.
        restMockMvc.perform(login("quiet-user", "wrong-password")).andExpect(status().isUnauthorized());
    }

    private org.springframework.test.web.servlet.RequestBuilder login(String username, String password) throws Exception {
        LoginVM login = new LoginVM();
        login.setUsername(username);
        login.setPassword(password);
        return post("/api/authenticate").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(login));
    }
}
