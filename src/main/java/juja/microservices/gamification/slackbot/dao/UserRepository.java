package juja.microservices.gamification.slackbot.dao;

import juja.microservices.gamification.slackbot.model.DTO.UserDTO;

import java.util.List;

/**
 * @author Artem
 */
public interface UserRepository {

    List<UserDTO> findUsersBySlackNames(List<String> slackNames);
}