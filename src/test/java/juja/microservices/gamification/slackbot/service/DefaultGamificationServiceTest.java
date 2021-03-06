package juja.microservices.gamification.slackbot.service;

import juja.microservices.gamification.slackbot.dao.GamificationRepository;
import juja.microservices.gamification.slackbot.model.DTO.UserDTO;
import juja.microservices.gamification.slackbot.model.SlackParsedCommand;
import juja.microservices.gamification.slackbot.model.achievements.CodenjoyAchievement;
import juja.microservices.gamification.slackbot.model.achievements.DailyAchievement;
import juja.microservices.gamification.slackbot.model.achievements.InterviewAchievement;
import juja.microservices.gamification.slackbot.model.achievements.ThanksAchievement;
import juja.microservices.gamification.slackbot.service.impl.SlackNameHandlerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Danil Kuznetsov
 * @author Nikolay Horushko
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class DefaultGamificationServiceTest {

    private final String FROM_USER_SLACK_NAME = "@from-user";
    @MockBean
    private GamificationRepository gamificationRepository;
    @MockBean
    private SlackNameHandlerService slackNameHandlerService;
    @Inject
    private GamificationService gamificationService;
    private UserDTO fromUser = new UserDTO("uuid-from-user", FROM_USER_SLACK_NAME);
    private UserDTO user1 = new UserDTO("uuid-user-1", "@slack1");
    private UserDTO user2 = new UserDTO("uuid-user-2", "@slack2");
    private UserDTO user3 = new UserDTO("uuid-user-3", "@slack3");

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldSaveNewDaily() {

        //given
        final String TEXT_COMMAND = "daily report";
        final String[] SAVED_ACHIEVEMENT_ID = {"100"};
        final String EXPECTED_RESPONSE_TO_SLACK = "Thanks, your daily report saved.";

        Map<String, UserDTO> users = new HashMap<>();
        users.put(fromUser.getSlack(), fromUser);

        when(slackNameHandlerService.createSlackParsedCommand(FROM_USER_SLACK_NAME, TEXT_COMMAND))
                .thenReturn(new SlackParsedCommand(fromUser.getSlack(), TEXT_COMMAND, users));
        when(gamificationRepository.saveDailyAchievement(any(DailyAchievement.class)))
                .thenReturn(SAVED_ACHIEVEMENT_ID);

        //when
        String result = gamificationService.sendDailyAchievement(FROM_USER_SLACK_NAME, TEXT_COMMAND);

        //then
        assertThat(result, equalTo(EXPECTED_RESPONSE_TO_SLACK));
        verify(slackNameHandlerService).createSlackParsedCommand(FROM_USER_SLACK_NAME, TEXT_COMMAND);
        verify(gamificationRepository).saveDailyAchievement(any(DailyAchievement.class));
        verifyNoMoreInteractions(slackNameHandlerService, gamificationRepository);
    }

    @Test
    public void saveNewDailyWhenRepositoryReturnNotOneIdShouldErrorMessage() {
        //given
        final String TEXT_COMMAND = "daily report";
        final String[] SAVED_ACHIEVEMENT_ID = {"100", "200"};
        final String EXPECTED_RESPONSE_TO_SLACK = "Something went wrong and we didn't save your daily report";

        Map<String, UserDTO> users = new HashMap<>();
        users.put(fromUser.getSlack(), fromUser);

        when(slackNameHandlerService.createSlackParsedCommand(FROM_USER_SLACK_NAME, TEXT_COMMAND))
                .thenReturn(new SlackParsedCommand(fromUser.getSlack(), TEXT_COMMAND, users));
        when(gamificationRepository.saveDailyAchievement(any(DailyAchievement.class)))
                .thenReturn(SAVED_ACHIEVEMENT_ID);

        //when
        String result = gamificationService.sendDailyAchievement(FROM_USER_SLACK_NAME, TEXT_COMMAND);

        //then
        assertThat(result, equalTo(EXPECTED_RESPONSE_TO_SLACK));
        verify(slackNameHandlerService).createSlackParsedCommand(FROM_USER_SLACK_NAME, TEXT_COMMAND);
        verify(gamificationRepository).saveDailyAchievement(any(DailyAchievement.class));
        verifyNoMoreInteractions(slackNameHandlerService, gamificationRepository);
    }

    @Test
    public void shouldSaveNewCodenjoy() {

        //given
        final String TEXT_COMMAND = "-1th @slack1 -2th @slack2 -3th @slack3";
        final String[] SAVED_ACHIEVEMENT_ID = {"100", "101", "102"};
        final String EXPECTED_RESPONSE_TO_SLACK = "Thanks, we awarded the users. " +
                "First place: @slack1, Second place: @slack2, Third place: @slack3";

        Map<String, UserDTO> users = new HashMap<>();
        users.put(fromUser.getSlack(), fromUser);
        users.put(user1.getSlack(), user1);
        users.put(user2.getSlack(), user2);
        users.put(user3.getSlack(), user3);

        when(slackNameHandlerService.createSlackParsedCommand(FROM_USER_SLACK_NAME, TEXT_COMMAND))
                .thenReturn(new SlackParsedCommand(fromUser.getSlack(), TEXT_COMMAND, users));
        when(gamificationRepository.saveCodenjoyAchievement(any(CodenjoyAchievement.class)))
                .thenReturn(SAVED_ACHIEVEMENT_ID);

        //when
        String result = gamificationService.sendCodenjoyAchievement(FROM_USER_SLACK_NAME, TEXT_COMMAND);

        //then
        assertThat(result, equalTo(EXPECTED_RESPONSE_TO_SLACK));
        verify(slackNameHandlerService).createSlackParsedCommand(FROM_USER_SLACK_NAME, TEXT_COMMAND);
        verify(gamificationRepository).saveCodenjoyAchievement(any(CodenjoyAchievement.class));
        verifyNoMoreInteractions(slackNameHandlerService, gamificationRepository);
    }

    @Test
    public void saveNewCodenjoyWhenRepositoryReturnNotThreeIdShouldErrorMessage() {
        //given
        final String TEXT_COMMAND = "-1th @slack1 -2th @slack2 -3th @slack3";
        final String[] SAVED_ACHIEVEMENT_ID = {"100", "101", "102", "104"};
        final String EXPECTED_RESPONSE_TO_SLACK = "Something went wrong and we didn't award the users";

        Map<String, UserDTO> users = new HashMap<>();
        users.put(fromUser.getSlack(), fromUser);
        users.put(user1.getSlack(), user1);
        users.put(user2.getSlack(), user2);
        users.put(user3.getSlack(), user3);

        when(slackNameHandlerService.createSlackParsedCommand(FROM_USER_SLACK_NAME, TEXT_COMMAND))
                .thenReturn(new SlackParsedCommand(fromUser.getSlack(), TEXT_COMMAND, users));
        when(gamificationRepository.saveCodenjoyAchievement(any(CodenjoyAchievement.class)))
                .thenReturn(SAVED_ACHIEVEMENT_ID);

        //when
        String result = gamificationService.sendCodenjoyAchievement(FROM_USER_SLACK_NAME, TEXT_COMMAND);

        //then
        assertThat(result, equalTo(EXPECTED_RESPONSE_TO_SLACK));
        verify(slackNameHandlerService).createSlackParsedCommand(FROM_USER_SLACK_NAME, TEXT_COMMAND);
        verify(gamificationRepository).saveCodenjoyAchievement(any(CodenjoyAchievement.class));
        verifyNoMoreInteractions(slackNameHandlerService, gamificationRepository);
    }

    @Test
    public void shouldSaveNewThanks() {

        //given
        final String TEXT_COMMAND = "thanks @slack1 comment";
        final String[] SAVED_ACHIEVEMENT_ID = {"100"};
        final String EXPECTED_RESPONSE_TO_SLACK = "Thanks, your 'thanks' for @slack1 saved.";

        Map<String, UserDTO> users = new HashMap<>();
        users.put(fromUser.getSlack(), fromUser);
        users.put(user1.getSlack(), user1);

        when(slackNameHandlerService.createSlackParsedCommand(FROM_USER_SLACK_NAME, TEXT_COMMAND))
                .thenReturn(new SlackParsedCommand(fromUser.getSlack(), TEXT_COMMAND, users));
        when(gamificationRepository.saveThanksAchievement(any(ThanksAchievement.class)))
                .thenReturn(SAVED_ACHIEVEMENT_ID);

        //when
        String result = gamificationService.sendThanksAchievement(FROM_USER_SLACK_NAME, TEXT_COMMAND);

        //then
        assertThat(result, equalTo(EXPECTED_RESPONSE_TO_SLACK));
        verify(slackNameHandlerService).createSlackParsedCommand(FROM_USER_SLACK_NAME, TEXT_COMMAND);
        verify(gamificationRepository).saveThanksAchievement(any(ThanksAchievement.class));
        verifyNoMoreInteractions(slackNameHandlerService, gamificationRepository);
    }

    @Test
    public void shouldSaveSecondThanksAndAddExtraScore() {

        //given
        final String TEXT_COMMAND = "thanks @slack1 comment";
        final String[] SAVED_ACHIEVEMENT_ID = {"100", "101"};
        final String EXPECTED_RESPONSE_TO_SLACK = "Thanks, your 'thanks' for @slack1 saved. " +
                "Also you received +1 for your activity.";

        Map<String, UserDTO> users = new HashMap<>();
        users.put(fromUser.getSlack(), fromUser);
        users.put(user1.getSlack(), user1);

        when(slackNameHandlerService.createSlackParsedCommand(FROM_USER_SLACK_NAME, TEXT_COMMAND))
                .thenReturn(new SlackParsedCommand(fromUser.getSlack(), TEXT_COMMAND, users));
        when(gamificationRepository.saveThanksAchievement(any(ThanksAchievement.class)))
                .thenReturn(SAVED_ACHIEVEMENT_ID);

        //when
        String result = gamificationService.sendThanksAchievement(FROM_USER_SLACK_NAME, TEXT_COMMAND);

        //then
        assertThat(result, equalTo(EXPECTED_RESPONSE_TO_SLACK));
        verify(slackNameHandlerService).createSlackParsedCommand(FROM_USER_SLACK_NAME, TEXT_COMMAND);
        verify(gamificationRepository).saveThanksAchievement(any(ThanksAchievement.class));
        verifyNoMoreInteractions(slackNameHandlerService, gamificationRepository);
    }

    @Test
    public void shouldSaveNewInterviewAchievement() {

        //given
        final String TEXT_COMMAND = "interview report";
        final String[] SAVED_ACHIEVEMENT_ID = {"100"};
        final String EXPECTED_RESPONSE_TO_SLACK = "Thanks. Your interview saved.";

        Map<String, UserDTO> users = new HashMap<>();
        users.put(fromUser.getSlack(), fromUser);

        when(slackNameHandlerService.createSlackParsedCommand(FROM_USER_SLACK_NAME, TEXT_COMMAND))
                .thenReturn(new SlackParsedCommand(fromUser.getSlack(), TEXT_COMMAND, users));
        when(gamificationRepository.saveInterviewAchievement(any(InterviewAchievement.class)))
                .thenReturn(SAVED_ACHIEVEMENT_ID);

        //when
        String result = gamificationService.sendInterviewAchievement(FROM_USER_SLACK_NAME, TEXT_COMMAND);

        //then
        assertThat(result, equalTo(EXPECTED_RESPONSE_TO_SLACK));
        verify(slackNameHandlerService).createSlackParsedCommand(FROM_USER_SLACK_NAME, TEXT_COMMAND);
        verify(gamificationRepository).saveInterviewAchievement(any(InterviewAchievement.class));
        verifyNoMoreInteractions(slackNameHandlerService, gamificationRepository);
    }

    @Test
    public void saveNewInterviewWhenRepositoryReturnNotOneIdShouldErrorMessage() {
        //given
        final String TEXT_COMMAND = "interview report";
        final String[] SAVED_ACHIEVEMENT_ID = {"100", "200"};
        final String EXPECTED_RESPONSE_TO_SLACK = "Something went wrong and we didn't save your interview";

        Map<String, UserDTO> users = new HashMap<>();
        users.put(fromUser.getSlack(), fromUser);

        when(slackNameHandlerService.createSlackParsedCommand(FROM_USER_SLACK_NAME, TEXT_COMMAND))
                .thenReturn(new SlackParsedCommand(fromUser.getSlack(), TEXT_COMMAND, users));
        when(gamificationRepository.saveInterviewAchievement(any(InterviewAchievement.class)))
                .thenReturn(SAVED_ACHIEVEMENT_ID);

        //when
        String result = gamificationService.sendInterviewAchievement(FROM_USER_SLACK_NAME, TEXT_COMMAND);

        //then
        assertThat(result, equalTo(EXPECTED_RESPONSE_TO_SLACK));
        verify(slackNameHandlerService).createSlackParsedCommand(FROM_USER_SLACK_NAME, TEXT_COMMAND);
        verify(gamificationRepository).saveInterviewAchievement(any(InterviewAchievement.class));
        verifyNoMoreInteractions(slackNameHandlerService, gamificationRepository);
    }
}
