package com.game.controller;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.service.IPlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
public class PlayerController {

    private IPlayerService playerService;

    @Autowired
    public void setPlayerService(IPlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping(value = "/rest/players")
    public ResponseEntity<?> findAll(@RequestParam(value = "name", required = false) String name,
                                      @RequestParam(value = "title", required = false) String title,
                                      @RequestParam(value = "race", required = false) Race race,
                                      @RequestParam(value = "profession", required = false) Profession profession,
                                      @RequestParam(value = "after", required = false) Long after,
                                      @RequestParam(value = "before", required = false) Long before,
                                      @RequestParam(value = "banned", required = false) Boolean banned,
                                      @RequestParam(value = "minExperience", required = false) Integer minExperience,
                                      @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
                                      @RequestParam(value = "minLevel", required = false) Integer minLevel,
                                      @RequestParam(value = "maxLevel", required = false) Integer maxLevel,
                                      @RequestParam(value = "order", required = false, defaultValue = "ID") PlayerOrder order,
                                      @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
                                      @RequestParam(value = "pageSize", required = false, defaultValue = "3") Integer pageSize) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(order.getFieldName()));

        Specification<Player> specification = Specification.where(playerService.selectByName(name)
                .and(playerService.selectByTitle(title))
                .and(playerService.selectByRace(race))
                .and(playerService.selectByProfession(profession))
                .and(playerService.selectByBan(banned))
                .and(playerService.selectByBirthDate(after, before))
                .and(playerService.selectByLvl(minLevel, maxLevel))
                .and(playerService.selectByExp(minExperience, maxExperience)));

        return new ResponseEntity<>(playerService.getPlayersList(specification, pageable).getContent(), HttpStatus.OK);
    }

    @GetMapping(value = "/rest/players/count")
    public ResponseEntity<?> getCount(@RequestParam(value = "name", required = false) String name,
                                      @RequestParam(value = "title", required = false) String title,
                                      @RequestParam(value = "race", required = false) Race race,
                                      @RequestParam(value = "profession", required = false) Profession profession,
                                      @RequestParam(value = "after", required = false) Long after,
                                      @RequestParam(value = "before", required = false) Long before,
                                      @RequestParam(value = "banned", required = false) Boolean banned,
                                      @RequestParam(value = "minExperience", required = false) Integer minExperience,
                                      @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
                                      @RequestParam(value = "minLevel", required = false) Integer minLevel,
                                      @RequestParam(value = "maxLevel", required = false) Integer maxLevel) {

        Specification<Player> specification = Specification.where(playerService.selectByName(name)
                .and(playerService.selectByTitle(title))
                .and(playerService.selectByRace(race))
                .and(playerService.selectByProfession(profession))
                .and(playerService.selectByBan(banned))
                .and(playerService.selectByBirthDate(after, before))
                .and(playerService.selectByLvl(minLevel, maxLevel))
                .and(playerService.selectByExp(minExperience, maxExperience)));

        return new ResponseEntity<>(playerService.getPlayersCount(specification), HttpStatus.OK);
    }

    @PostMapping(value = "/rest/players")
    public ResponseEntity<Player> createPlayer(@RequestBody Player player) {
        return new ResponseEntity<>(playerService.createPlayer(player), HttpStatus.OK);
    }

    @GetMapping(value = "/rest/players/{id}")
    public ResponseEntity<?> findPlayerById(@PathVariable String id) {
        Long longId = playerService.checkId(id);
        return new ResponseEntity<>(playerService.getPlayer(longId), HttpStatus.OK);
    }

    @DeleteMapping(value = "/rest/players/{id}")
    public ResponseEntity<?> deletePlayer(@PathVariable String id) {
        Long longId = playerService.checkId(id);
        playerService.deletePlayer(longId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(value = "/rest/players/{id}")
    public ResponseEntity<Player> updatePlayer(@PathVariable String id, @RequestBody Player player) {
        Player responsePlayer;
        Long longId = playerService.checkId(id);
        responsePlayer = playerService.updatePlayer(longId, player);
        return new ResponseEntity<>(responsePlayer, HttpStatus.OK);
    }

}
