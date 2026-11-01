
package cl.salfa.rabbitadmin;

import cl.salfa.rabbitadmin.service.RabbitAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RabbitAdminSecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RabbitAdminService rabbitAdminService;

    private static final String ENDPOINT =
            "/api/rabbit-admin/queues";

    private static final String BODY =
            "{\"name\":\"dyn.security.test\"}";

    @Test
    void withoutTokenShouldReturn401() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isUnauthorized());

        verify(rabbitAdminService, never())
                .createQueue(any());
    }

    @Test
    void userWithoutAdminRoleShouldReturn403()
            throws Exception {

        mockMvc.perform(post(ENDPOINT)
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_USER")
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isForbidden());

        verify(rabbitAdminService, never())
                .createQueue(any());
    }

    @Test
    void adminShouldAccessEndpoint()
            throws Exception {

        mockMvc.perform(post(ENDPOINT)
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isCreated());

        verify(rabbitAdminService)
                .createQueue(any());
    }
}
