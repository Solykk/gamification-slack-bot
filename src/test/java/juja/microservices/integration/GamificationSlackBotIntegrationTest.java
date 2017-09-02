package juja.microservices.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import juja.microservices.gamification.slackbot.GamificationSlackBotApplication;
import juja.microservices.gamification.slackbot.model.DTO.UserDTO;
import juja.microservices.utils.SlackUrlUtils;
import me.ramswaroop.jbot.core.slack.models.RichMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;

/**
 * @author Nikolay Horushko
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {GamificationSlackBotApplication.class})
@AutoConfigureMockMvc
public class GamificationSlackBotIntegrationTest {
    private final static String INSTANT_MESSAGE = "Your command accepted. Please wait...";
    private final static String CODENJOY_THANKS_MESSAGE = "Thanks, we awarded the users. " +
            "First place: %s, Second place: %s, Third place: %s";
    private final static String DAILY_THANKS_MESSAGE = "Thanks, your daily report saved.";
    private final static String THANKS_ONE_THANKS_MESSAGE = "Thanks, your 'thanks' for %s saved.";
    private final static String INTERVIEW_THANKS_MESSAGE = "Thanks. Your interview saved.";
    private final static String responseUrl = "http://example.com";

    @Inject
    private RestTemplate restTemplate;

    @Inject
    private MockMvc mvc;
    private MockRestServiceServer mockServer;

    @Value("${gamification.baseURL}")
    private String urlBaseGamification;
    @Value("${endpoint.daily}")
    private String urlSendDaily;
    @Value("${endpoint.codenjoy}")
    private String urlSendCodenjoy;
    @Value("${endpoint.thanks}")
    private String urlSendThanks;
    @Value("${endpoint.interview}")
    private String urlSendInterview;

    @Value("${user.baseURL}")
    private String urlBaseUser;
    @Value("${endpoint.usersBySlackNames}")
    private String urlGetUser;

    private UserDTO user1 = new UserDTO("f2034f22-562b-4e02-bfcf-ec615c1ba62b", "@slack1");
    private UserDTO user2 = new UserDTO("f2034f33-563c-4e03-bfcf-ec615c1ba63c", "@slack2");
    private UserDTO user3 = new UserDTO("f2034f44-563d-4e04-bfcf-ec615c1ba64d", "@slack3");
    private UserDTO userFrom = new UserDTO("f2034f11-561a-4e01-bfcf-ec615c1ba61a", "@from-user");

    @Before
    public void setup() {
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
    }

    @Test
    public void onReceiveSlashCommandCodenjoyReturnOkRichMessage() throws Exception {
        final String CODENJOY_COMMAND_FROM_SLACK = "-1th @slack1 -2th @slack2 -3th @slack3";
        final List<UserDTO> usersInCommand = Arrays.asList(new UserDTO[]{user1, user2, user3, userFrom});
        mockSuccessUsersService(usersInCommand);

        final String EXPECTED_REQUEST_TO_GAMIFICATION = String.format("{\"from\":\"%s\",\"firstPlace\":\"%s\"," +
                        "\"secondPlace\":\"%s\",\"thirdPlace\":\"%s\"}", usersInCommand.get(3).getUuid(),
                usersInCommand.get(0).getUuid(), usersInCommand.get(1).getUuid(), usersInCommand.get(2).getUuid());

        final String EXPECTED_RESPONSE_FROM_GAMIFICATION = "[\"101\", \"102\", \"103\"]";

        mockSuccessGamificationService(urlBaseGamification + urlSendCodenjoy, EXPECTED_REQUEST_TO_GAMIFICATION,
                EXPECTED_RESPONSE_FROM_GAMIFICATION);
        mockSlackResponseUrl(responseUrl, new RichMessage(String.format(CODENJOY_THANKS_MESSAGE,
                user1.getSlack(), user2.getSlack(), user3.getSlack())));

        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/codenjoy"),
                SlackUrlUtils.getUriVars("slashCommandToken", "/codenjoy", CODENJOY_COMMAND_FROM_SLACK))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string(INSTANT_MESSAGE));
    }

    @Test
    public void returnOkMessageIfCondejoyCommandTokensWithoutSpaces() throws Exception {
        final String CODENJOY_COMMAND_FROM_SLACK = "-1th@slack1 -2th@slack2 -3th @slack3";
        final List<UserDTO> usersInCommand = Arrays.asList(new UserDTO[]{user1, user2, user3, userFrom});
        mockSuccessUsersService(usersInCommand);

        final String EXPECTED_REQUEST_TO_GAMIFICATION = String.format("{\"from\":\"%s\",\"firstPlace\":\"%s\"," +
                        "\"secondPlace\":\"%s\",\"thirdPlace\":\"%s\"}", usersInCommand.get(3).getUuid(),
                usersInCommand.get(0).getUuid(), usersInCommand.get(1).getUuid(), usersInCommand.get(2).getUuid());
        final String EXPECTED_RESPONSE_FROM_GAMIFICATION = "[\"101\", \"102\", \"103\"]";

        mockSuccessGamificationService(urlBaseGamification + urlSendCodenjoy, EXPECTED_REQUEST_TO_GAMIFICATION,
                EXPECTED_RESPONSE_FROM_GAMIFICATION);
        mockSlackResponseUrl(responseUrl, new RichMessage(String.format(CODENJOY_THANKS_MESSAGE,
                user1.getSlack(), user2.getSlack(), user3.getSlack())));

        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/codenjoy"),
                SlackUrlUtils.getUriVars("slashCommandToken", "/codenjoy", CODENJOY_COMMAND_FROM_SLACK))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string(INSTANT_MESSAGE));
    }

    @Test
    public void returnOkMessageIfCondejoyCommandTokensInWrongOrder() throws Exception {
        final String CODENJOY_COMMAND_FROM_SLACK = "-2th @slack2 -1th @slack1 -3th @slack3";
        final List<UserDTO> usersInCommand = Arrays.asList(new UserDTO[]{user2, user1, user3, userFrom});
        mockSuccessUsersService(usersInCommand);

        final String EXPECTED_REQUEST_TO_GAMIFICATION = String.format("{\"from\":\"%s\",\"firstPlace\":\"%s\"," +
                        "\"secondPlace\":\"%s\",\"thirdPlace\":\"%s\"}", usersInCommand.get(3).getUuid(),
                usersInCommand.get(1).getUuid(), usersInCommand.get(0).getUuid(), usersInCommand.get(2).getUuid());
        final String EXPECTED_RESPONSE_FROM_GAMIFICATION = "[\"101\", \"102\", \"103\"]";

        mockSuccessGamificationService(urlBaseGamification + urlSendCodenjoy, EXPECTED_REQUEST_TO_GAMIFICATION,
                EXPECTED_RESPONSE_FROM_GAMIFICATION);
        mockSlackResponseUrl(responseUrl, new RichMessage(String.format(CODENJOY_THANKS_MESSAGE,
                user1.getSlack(), user2.getSlack(), user3.getSlack())));

        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/codenjoy"),
                SlackUrlUtils.getUriVars("slashCommandToken", "/codenjoy", CODENJOY_COMMAND_FROM_SLACK))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string(INSTANT_MESSAGE));
    }

    @Test
    public void returnErrorMessageIfCondejoyCommandWithout2thToken() throws Exception {
        final String CODENJOY_COMMAND_FROM_SLACK = "-1th @slack1 @slack2 -3th @slack3";
        final List<UserDTO> usersInCommand = Arrays.asList(new UserDTO[]{user1, user2, user3, userFrom});
        mockSuccessUsersService(usersInCommand);

        final String EXPECTED_RESPONSE_TO_SLACK = "Token '-2th' didn't find in the string '-1th @slack1 @slack2 " +
                "-3th @slack3'";
        mockSlackResponseUrl(responseUrl, new RichMessage(EXPECTED_RESPONSE_TO_SLACK));

        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/codenjoy"),
                SlackUrlUtils.getUriVars("slashCommandToken", "/codenjoy", CODENJOY_COMMAND_FROM_SLACK))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string(INSTANT_MESSAGE));
    }

    @Test
    public void onReceiveSlashCommandDailyReturnOkRichMessage() throws Exception {
        final String DAILY_COMMAND_TEXT_FROM_SLACK = "I did smth today";
        final List<UserDTO> usersInCommand = Arrays.asList(new UserDTO[]{userFrom});
        mockSuccessUsersService(usersInCommand);

        final String EXPECTED_REQUEST_TO_GAMIFICATION = String.format("{\"description\":\"%s\",\"from\":\"%s\"}",
                DAILY_COMMAND_TEXT_FROM_SLACK, usersInCommand.get(0).getUuid());
        final String EXPECTED_RESPONSE_FROM_GAMIFICATION = "[\"101\"]";

        mockSuccessGamificationService(urlBaseGamification + urlSendDaily, EXPECTED_REQUEST_TO_GAMIFICATION,
                EXPECTED_RESPONSE_FROM_GAMIFICATION);
        mockSlackResponseUrl(responseUrl, new RichMessage(DAILY_THANKS_MESSAGE));

        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/daily"),
                SlackUrlUtils.getUriVars("slashCommandToken", "/codenjoy", DAILY_COMMAND_TEXT_FROM_SLACK))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string(INSTANT_MESSAGE));
    }

    @Test
    public void onReceiveSlashCommandInterviewReturnOkRichMessage() throws Exception {
        final String INTERVIEW_COMMAND_TEXT_FROM_SLACK = "I went to an interview yesterday and got offer";
        final List<UserDTO> usersInCommand = Arrays.asList(new UserDTO[]{userFrom});
        mockSuccessUsersService(usersInCommand);

        final String EXPECTED_REQUEST_TO_GAMIFICATION = String.format("{\"description\":\"%s\",\"from\":\"%s\"}",
                INTERVIEW_COMMAND_TEXT_FROM_SLACK, usersInCommand.get(0).getUuid());
        final String EXPECTED_RESPONSE_FROM_GAMIFICATION = "[\"101\"]";

        mockSuccessGamificationService(urlBaseGamification + urlSendInterview, EXPECTED_REQUEST_TO_GAMIFICATION,
                EXPECTED_RESPONSE_FROM_GAMIFICATION);
        mockSlackResponseUrl(responseUrl, new RichMessage(INTERVIEW_THANKS_MESSAGE));

        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/interview"),
                SlackUrlUtils.getUriVars("slashCommandToken", "/interview", INTERVIEW_COMMAND_TEXT_FROM_SLACK))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string(INSTANT_MESSAGE));
    }

    @Test
    public void onReceiveSlashCommandThanksReturnOkRichMessage() throws Exception {
        final String THANKS_COMMAND_TEXT_FROM_SLACK = "@slack1 thanks for your help!";
        final List<UserDTO> usersInCommand = Arrays.asList(new UserDTO[]{user1, userFrom});
        mockSuccessUsersService(usersInCommand);

        final String EXPECTED_REQUEST_TO_GAMIFICATION = String.format("{\"description\":\"%s\",\"from\":\"%s\",\"to\":\"%s\"}",
                user1.getSlack() + " thanks for your help!", usersInCommand.get(1).getUuid(), usersInCommand.get(0).getUuid());
        final String EXPECTED_RESPONSE_FROM_GAMIFICATION = "[\"101\"]";

        mockSuccessGamificationService(urlBaseGamification + urlSendThanks, EXPECTED_REQUEST_TO_GAMIFICATION,
                EXPECTED_RESPONSE_FROM_GAMIFICATION);
        mockSlackResponseUrl(responseUrl, new RichMessage(String.format(THANKS_ONE_THANKS_MESSAGE, user1.getSlack())));

        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/thanks"),
                SlackUrlUtils.getUriVars("slashCommandToken", "/thanks", THANKS_COMMAND_TEXT_FROM_SLACK))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string(INSTANT_MESSAGE));
    }

    @Test
    public void returnErrorMessageIfThanksCommandConsistTwoOrMoreSlackNames() throws Exception {
        final String THANKS_COMMAND_TEXT_FROM_SLACK = "@slack1 thanks @slack2 for your help!";
        final List<UserDTO> usersInCommand = Arrays.asList(new UserDTO[]{user1, user2, userFrom});
        mockSuccessUsersService(usersInCommand);

        final String EXPECTED_RESPONSE_TO_SLACK = "We found 2 slack names in your command: '@slack1 thanks " +
                "@slack2 for your help!'  You can't send thanks more than one user.";
        mockSlackResponseUrl(responseUrl, new RichMessage(EXPECTED_RESPONSE_TO_SLACK));

        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/thanks"),
                SlackUrlUtils.getUriVars("slashCommandToken", "/thanks", THANKS_COMMAND_TEXT_FROM_SLACK))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string(INSTANT_MESSAGE));
    }

    @Test
    public void returnClientErrorMessageWhenUserServiceIsFail() throws Exception {
        final String THANKS_COMMAND_TEXT_FROM_SLACK = "@slack1 thanks @slack2 for your help!";
        final List<UserDTO> usersInCommand = Arrays.asList(new UserDTO[]{user1, user2, userFrom});
        mockFailUsersService(usersInCommand);

        final String EXPECTED_RESPONSE_TO_SLACK = "Oops something went wrong :(";
        mockSlackResponseUrl(responseUrl, new RichMessage(EXPECTED_RESPONSE_TO_SLACK));

        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/thanks"),
                SlackUrlUtils.getUriVars("slashCommandToken", "/thanks", THANKS_COMMAND_TEXT_FROM_SLACK))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string(INSTANT_MESSAGE));
    }


    @Test
    public void returnClientErrorMessageWhenGamificationServiceIsFail() throws Exception {
        final String THANKS_COMMAND_TEXT_FROM_SLACK = "@slack1 thanks for your help!";
        final List<UserDTO> usersInCommand = Arrays.asList(new UserDTO[]{user1, userFrom});
        mockSuccessUsersService(usersInCommand);

        final String EXPECTED_REQUEST_TO_GAMIFICATION = String.format("{\"description\":\"%s\",\"from\":\"%s\",\"to\":\"%s\"}",
                user1.getSlack() + " thanks for your help!", usersInCommand.get(1).getUuid(), usersInCommand.get(0).getUuid());

        mockFailGamificationService(urlBaseGamification + urlSendThanks, EXPECTED_REQUEST_TO_GAMIFICATION);

        final String EXPECTED_RESPONSE_TO_SLACK = "Oops something went wrong :(";
        mockSlackResponseUrl(responseUrl, new RichMessage(EXPECTED_RESPONSE_TO_SLACK));

        mvc.perform(MockMvcRequestBuilders.post(SlackUrlUtils.getUrlTemplate("/commands/thanks"),
                SlackUrlUtils.getUriVars("slashCommandToken", "/thanks", THANKS_COMMAND_TEXT_FROM_SLACK))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string(INSTANT_MESSAGE));
    }

    private void mockFailUsersService(List<UserDTO> users) throws JsonProcessingException {
        List<String> slackNames = new ArrayList<>();
        for (UserDTO user : users) {
            slackNames.add(user.getSlack());
        }
        ObjectMapper mapper = new ObjectMapper();
        mockServer.expect(requestTo(urlBaseUser + urlGetUser))
                .andExpect(method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(MockRestRequestMatchers.content().string(String.format("{\"slackNames\":%s}", mapper.writeValueAsString(slackNames))))
                .andRespond(withBadRequest().body("{\"httpStatus\":400,\"internalErrorCode\":1," +
                        "\"clientMessage\":\"Oops something went wrong :(\"," +
                        "\"developerMessage\":\"General exception for this service\"," +
                        "\"exceptionMessage\":\"very big and scare error\",\"detailErrors\":[]}"));
    }

    private void mockFailGamificationService(String expectedURI, String expectedRequestBody) {
        mockServer.expect(requestTo(expectedURI))
                .andExpect(method(HttpMethod.POST))
                .andExpect(request -> assertThat(request.getHeaders().getContentType().toString(),
                        containsString("application/json")))
                .andExpect(request -> assertThat(request.getBody().toString(), equalTo(expectedRequestBody)))
                .andRespond(withBadRequest().body("{\"httpStatus\":400,\"internalErrorCode\":1," +
                        "\"clientMessage\":\"Oops something went wrong :(\"," +
                        "\"developerMessage\":\"General exception for this service\"," +
                        "\"exceptionMessage\":\"very big and scare error\",\"detailErrors\":[]}"));
    }

    private void mockSuccessUsersService(List<UserDTO> users) throws JsonProcessingException {
        List<String> slackNames = new ArrayList<>();
        for (UserDTO user : users) {
            slackNames.add(user.getSlack());
        }
        ObjectMapper mapper = new ObjectMapper();
        mockServer.expect(requestTo(urlBaseUser + urlGetUser))
                .andExpect(method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(MockRestRequestMatchers.content().string(String.format("{\"slackNames\":%s}", mapper.writeValueAsString(slackNames))))
                .andRespond(withSuccess(mapper.writeValueAsString(users), MediaType.APPLICATION_JSON_UTF8));
    }

    private void mockSuccessGamificationService(String expectedURI, String expectedRequestBody, String response) {
        mockServer.expect(requestTo(expectedURI))
                .andExpect(method(HttpMethod.POST))
                .andExpect(request -> assertThat(request.getHeaders().getContentType().toString(),
                        containsString("application/json")))
                .andExpect(request -> assertThat(request.getBody().toString(), equalTo(expectedRequestBody)))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));
    }

    private void mockSlackResponseUrl(String expectedURI, RichMessage delayedMessage) {
        ObjectMapper mapper = new ObjectMapper();
        mockServer.expect(requestTo(expectedURI))
                .andExpect(method(HttpMethod.POST))
                .andExpect(request -> assertThat(request.getHeaders().getContentType().toString(),
                        containsString("application/json")))
                .andExpect(request -> assertThatJson(request.getBody().toString())
                        .isEqualTo(mapper.writeValueAsString(delayedMessage)))
                .andRespond(withSuccess("OK", MediaType.APPLICATION_FORM_URLENCODED));
    }
}
