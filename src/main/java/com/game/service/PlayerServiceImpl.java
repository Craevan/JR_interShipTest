package com.game.service;


import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;
import com.game.service.exception.BadRequestException;
import com.game.service.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

@Service
@Transactional
public class PlayerServiceImpl implements IPlayerService {

    @Autowired
    private PlayerRepository playerRepository;

    @Override
    public Page<Player> getPlayersList(Specification<Player> specification, Pageable sortedBy) {
        return playerRepository.findAll(specification, sortedBy);
    }

    @Override
    public Integer getPlayersCount(Specification<Player> specification) {
        return playerRepository.findAll(specification).size();
    }

    @Override
    public Player createPlayer(Player player) {
        if (player.getName() == null || player.getTitle() == null || player.getRace() == null
            || player.getProfession() == null || player.getBirthday() == null
            || player.getBanned() == null || player.getExperience() == null) {
            throw new BadRequestException();
        }

        checkPlayerName(player);
        checkPlayerTitle(player);
        checkPlayerBirthDate(player);
        checkPlayerExperience(player);

        if (player.getBanned() == null) {
            player.setBanned(false);
        }

        Integer lvl = calculateLvl(player);
        player.setLevel(lvl);

        Integer untilNextLevel = calculateUntilNextLevel(player);
        player.setUntilNextLevel(untilNextLevel);

        return playerRepository.save(player);
    }

    @Override
    public Player getPlayer(Long id) {
        if (!playerRepository.existsById(id)) {
            throw new NotFoundException();
        }
        return playerRepository.findById(id).get();
    }

    @Override
    public Long checkId(String id) {
        Long longId = null;

        if (id == null || id.equals("") || id.equals("0")) {
            throw new BadRequestException();
        }

        try {
            longId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new BadRequestException();
        }

        if (longId < 0) {
            throw new BadRequestException();
        }

        return longId;
    }

    @Override
    public Player updatePlayer(Long id, Player player) {
        Player editedPlayer = getPlayer(id);

        String name = player.getName();
        if (name != null) {
            checkPlayerName(player);
            editedPlayer.setName(name);
        }

        String title = player.getTitle();
        if (title != null) {
            checkPlayerTitle(player);
            editedPlayer.setTitle(title);
        }

        Race race = player.getRace();
        if (race != null) {
            editedPlayer.setRace(race);
        }

        Profession profession = player.getProfession();
        if (profession != null) {
            editedPlayer.setProfession(profession);
        }

        Date bd = player.getBirthday();
        if (bd != null) {
            checkPlayerBirthDate(player);
            editedPlayer.setBirthday(bd);
        }

        Boolean ban = player.getBanned();
        if (ban != null) {
            editedPlayer.setBanned(ban);
        }

        Integer exp = player.getExperience();
        if (exp != null) {
            checkPlayerExperience(player);
            editedPlayer.setExperience(exp);
        }

        Integer newLvl = calculateLvl(editedPlayer);
        editedPlayer.setLevel(newLvl);

        Integer expUntilNextLvl = calculateUntilNextLevel(editedPlayer);
        editedPlayer.setUntilNextLevel(expUntilNextLvl);

        return playerRepository.save(editedPlayer);

    }

    @Override
    public void deletePlayer(Long id) {
        if (!playerRepository.existsById(id)) {
            throw new NotFoundException();
        }

        playerRepository.deleteById(id);
    }

    @Override
    public Specification<Player> selectByName(String name) {
        return new Specification<Player>() {
            @Override
            public Predicate toPredicate(Root<Player> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                if (name == null) {
                    return null;
                }
                return criteriaBuilder.like(root.get("name"), "%" + name + "%");
            }
        };
    }

    @Override
    public Specification<Player> selectByTitle(String title) {
        return new Specification<Player>() {
            @Override
            public Predicate toPredicate(Root<Player> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                if (title == null) {
                    return null;
                }
                return criteriaBuilder.like(root.get("title"), "%" + title + "%");
            }
        };
    }

    @Override
    public Specification<Player> selectByRace(Race race) {
        return new Specification<Player>() {
            @Override
            public Predicate toPredicate(Root<Player> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                if (race == null) {
                    return null;
                }
                return criteriaBuilder.equal(root.get("race"), race);
            }
        };
    }

    @Override
    public Specification<Player> selectByProfession(Profession profession) {
        return new Specification<Player>() {
            @Override
            public Predicate toPredicate(Root<Player> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                if (profession == null) {
                    return null;
                }
                return criteriaBuilder.equal(root.get("profession"), profession);
            }
        };
    }

    @Override
    public Specification<Player> selectByBirthDate(Long after, Long before) {
        return new Specification<Player>() {
            @Override
            public Predicate toPredicate(Root<Player> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                if (after == null && before == null) {
                    return null;
                }

                if (after == null) {
                    Date tempBefore = new Date(before);
                    return criteriaBuilder.lessThanOrEqualTo(root.get("birthday"), tempBefore);
                }

                if (before == null) {
                    Date tempAfter = new Date(after);
                    return criteriaBuilder.greaterThanOrEqualTo(root.get("birthday"), tempAfter);
                }

                Calendar beforeCalendar = new GregorianCalendar();
                beforeCalendar.setTime(new Date(before));
                beforeCalendar.set(Calendar.HOUR, 0);
                beforeCalendar.add(Calendar.MILLISECOND, -1);

                Date tempAfter = new Date(after);
                Date tempBefore = beforeCalendar.getTime();

                return criteriaBuilder.between(root.get("birthday"), tempAfter, tempBefore);
            }
        };
    }

    @Override
    public Specification<Player> selectByBan(Boolean isBanned) {
        return new Specification<Player>() {
            @Override
            public Predicate toPredicate(Root<Player> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                if (isBanned == null) {
                    return null;
                }
                if (isBanned) {
                    return criteriaBuilder.isTrue(root.get("banned"));
                } else {
                    return criteriaBuilder.isFalse(root.get("banned"));
                }
            }
        };
    }

    @Override
    public Specification<Player> selectByExp(Integer minExperience, Integer maxExperience) {
        return new Specification<Player>() {
            @Override
            public Predicate toPredicate(Root<Player> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                if (minExperience == null && maxExperience == null) {
                    return null;
                }
                if (minExperience == null) {
                    return criteriaBuilder.lessThanOrEqualTo(root.get("experience"), maxExperience);
                }
                if (maxExperience == null) {
                    return criteriaBuilder.greaterThanOrEqualTo(root.get("experience"), minExperience);
                }
                return criteriaBuilder.between(root.get("experience"), minExperience, maxExperience);
            }
        };
    }

    @Override
    public Specification<Player> selectByLvl(Integer minLevel, Integer maxLevel) {
        return new Specification<Player>() {
            @Override
            public Predicate toPredicate(Root<Player> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                if (minLevel == null && maxLevel == null) {
                    return null;
                }
                if (minLevel == null) {
                    return criteriaBuilder.lessThanOrEqualTo(root.get("level"), maxLevel);
                }
                if (maxLevel == null) {
                    return criteriaBuilder.greaterThanOrEqualTo(root.get("level"), minLevel);
                }
                return criteriaBuilder.between(root.get("level"), minLevel, maxLevel);
            }
        };
    }

    private void checkPlayerName(Player player) {
        String name = player.getName();
        if (name.length() < 1 || name.length() > 12) {
            throw new BadRequestException();
        }
    }

    private void checkPlayerTitle(Player player) {
        String playerTitle = player.getTitle();
        if (playerTitle.length() < 1 || playerTitle.length() > 30) {
            throw new BadRequestException();
        }
    }

    private void checkPlayerBirthDate(Player player) {
        Date birthDate = player.getBirthday();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(birthDate);
        int year = calendar.get(Calendar.YEAR);
        if (year < 2000 || year > 3000) {
            throw new BadRequestException();
        }
    }

    private void checkPlayerExperience(Player player) {
        Integer speed = player.getExperience();
        if (speed < 0 || speed > 10_000_000) {
            throw new BadRequestException();
        }
    }

    private Integer calculateUntilNextLevel(Player player) {
        return (50 * (player.getLevel() + 1) * (player.getLevel() + 2)) - player.getExperience();
    }

    private Integer calculateLvl(Player player) {
        return (int) ((Math.sqrt(2500 + 200 * player.getExperience()) - 50) / 100);
    }

}
