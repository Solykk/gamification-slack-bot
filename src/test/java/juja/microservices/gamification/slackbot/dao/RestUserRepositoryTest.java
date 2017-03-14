package juja.microservices.gamification.slackbot.dao;

import juja.microservices.gamification.slackbot.exceptions.GamificationExchangeException;
import juja.microservices.gamification.slackbot.model.User;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Created by Artem
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class RestUserRepositoryTest {

    @Inject
    private UserRepository userRepository;

    @Inject
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() {
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
    }


    @Test
    public void shouldReturnUserWhenSendUserDataToRemoteUserService() {
        //given
        String expectedRequestBody = "/users/search/slackNickname=@user";
        String expectedRequestHeader = "application/json";
        mockServer.expect(requestTo("/users/search/slackNickname=@user"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(request -> assertThat(request.getHeaders().getContentType().toString(), containsString(expectedRequestHeader)))
                .andExpect(request -> assertThat(request.getBody().toString(), equalTo(expectedRequestBody)))
                .andRespond(withSuccess("{\"uuid\":\"a1b\",\"gmail\":\"mail@gmail.com\",\"slack\":\"@user\",\"skype\":\"user_skype\",\"linkedin\":\"user.linkedin\"," +
                        "\"facebook\":\"user.facebook\",\"twitter\":\"user.twitter\"}",MediaType.APPLICATION_JSON));

        //when
        User result = userRepository.findUserBySlack("@user");

        // then
        mockServer.verify();
        assertThat(result,equalTo(new User("a1b","mail@gmail.com","@user","user_skype","user.linkedin","user.facebook","user.twitter")));
    }

 /*   @Test
    public void shouldThrowExceptionWhenSendSearchUserToRemoteUserServiceThrowException() {
        // given
        String expectedRequestBody = "";
        String expectedRequestHeader = "application/json";
        mockServer.expect(requestTo("/users/search"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(request -> assertThat(request.getHeaders().getContentType().toString(), containsString(expectedRequestHeader)))
                .andExpect(request -> assertThat(request.getBody().toString(), equalTo(expectedRequestBody)))
                .andRespond(withBadRequest().body("bad request"));
        //then
        thrown.expect(GamificationExchangeException.class);
        thrown.expectMessage(containsString("User Exchange Error"));
        //when
        userRepository.findUserBySlack();
    }*/
}