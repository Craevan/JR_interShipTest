package com.game.service;


import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface IPlayerService {

    Page<Player> getPlayersList(Specification<Player> specification, Pageable sortedBy);
    Integer getPlayersCount(Specification<Player> specification);
    Player createPlayer(Player player);
    Player getPlayer(Long id);
    Long checkId(String id);
    Player updatePlayer(Long id, Player player);
    void deletePlayer(Long id);

    Specification<Player> selectByName(String name);
    Specification<Player> selectByTitle(String title);
    Specification<Player> selectByRace(Race race);
    Specification<Player> selectByProfession(Profession profession);
    Specification<Player> selectByBirthDate(Long after, Long before);
    Specification<Player> selectByBan(Boolean isBanned);
    Specification<Player> selectByExp(Integer minExperience, Integer maxExperience);
    Specification<Player> selectByLvl(Integer minLevel, Integer maxLevel);

}
