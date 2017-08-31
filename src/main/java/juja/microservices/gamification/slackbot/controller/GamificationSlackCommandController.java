package juja.microservices.gamification.slackbot.controller;

import juja.microservices.gamification.slackbot.service.GamificationService;
import me.ramswaroop.jbot.core.slack.models.RichMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

/**
 * @author Nikolay Horushko
 */
@RestController
public class GamificationSlackCommandController {
    @Value("${slack.slashCommandToken}")
    private String slackToken;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private GamificationService gamificationService;

    @Inject
    public GamificationSlackCommandController(GamificationService gamificationService) {
        this.gamificationService = gamificationService;
    }

    @RequestMapping(value = "/commands/codenjoy",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public RichMessage onReceiveSlashCommandCodenjoy(@RequestParam("token") String token,
                                                     @RequestParam("user_name") String fromUser,
                                                     @RequestParam("text") String text) {

        logger.debug("Received slash command Condenjoy achievement: from user: [{}] command: [{}] token: [{}]",
                fromUser, text, token);

        if (!token.equals(slackToken)) {
            logger.warn("Received invalid slack token: [{}] in command Codenjoy for user: [{}]", token, fromUser);
            return getRichMessageInvalidSlackCommand();
        }

        String responseToSlack = gamificationService.sendCodenjoyAchievement(fromUser, text);

        logger.info("Codenjoy command processed : user: [{}] text: [{}] and sent response into slack: [{}]",
                fromUser, text, responseToSlack);

        return new RichMessage(responseToSlack);
    }

    @RequestMapping(value = "/commands/daily",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public RichMessage onReceiveSlashCommandDaily(@RequestParam("token") String token,
                                                  @RequestParam("user_name") String fromUser,
                                                  @RequestParam("text") String text) {

        logger.debug("Received slash command Daily achievement: user: [{}] command: [{}] token: [{}]",
                fromUser, text, token);

        if (!token.equals(slackToken)) {
            logger.warn("Received invalid slack token: [{}] in command Daily for user: [{}] ", token, fromUser);
            return getRichMessageInvalidSlackCommand();
        }

        String responseToSlack = gamificationService.sendDailyAchievement(fromUser, text);

        logger.info("Daily command processed : user: [{}] text: [{}] and sent response into slack: [{}]",
                fromUser, text, responseToSlack);

        return new RichMessage(responseToSlack);
    }

    @RequestMapping(value = "/commands/thanks",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public RichMessage onReceiveSlashCommandThanks(@RequestParam("token") String token,
                                                   @RequestParam("user_name") String fromUser,
                                                   @RequestParam("text") String text) {

        logger.debug("Received slash command Thanks achievement: user: [{}] command: [{}] token: [{}]",
                fromUser, text, token);
        if (!token.equals(slackToken)) {
            logger.warn("Received invalid slack token: [{}] in command thanks for user: [{}]", token, fromUser);
            return getRichMessageInvalidSlackCommand();
        }

        String responseToSlack = gamificationService.sendThanksAchievement(fromUser, text);

        logger.info("Thanks command processed : user: [{}] text: [{}] and sent response into slack: [{}]",
                fromUser, text, responseToSlack);

        return new RichMessage(responseToSlack);
    }

    @RequestMapping(value = "/commands/interview",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public RichMessage onReceiveSlashCommandInterview(@RequestParam("token") String token,
                                                      @RequestParam("user_name") String fromUser,
                                                      @RequestParam("text") String text) {

        logger.debug("Received slash command Interview achievement: user: [{}] command: [{}] token: [{}]",
                fromUser, text, token);

        if (!token.equals(slackToken)) {
            logger.warn("Received invalid slack token: [{}] in command interview for user: [{}]", token, fromUser);
            return getRichMessageInvalidSlackCommand();
        }

        String responseToSlack = gamificationService.sendInterviewAchievement(fromUser, text);

        logger.info("Interview command processed : user: [{}] text: [{}] and sent response into slack: [{}]",
                fromUser, text, responseToSlack);
        return new RichMessage(responseToSlack);
    }

    private RichMessage getRichMessageInvalidSlackCommand() {
        return new RichMessage("Sorry! You're not lucky enough to use our slack command.");
    }
}