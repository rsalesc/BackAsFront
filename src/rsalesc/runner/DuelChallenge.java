/*
 * Copyright (c) 2017. Roberto Sales @ rsalesc
 *
 * This software is provided 'as-is', without any express or implied
 * warranty. In no event will the authors be held liable for any damages
 * arising from the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 *    1. The origin of this software must not be misrepresented; you must not
 *    claim that you wrote the original software. If you use this software
 *    in a product, an acknowledgment in the product documentation would be
 *    appreciated but is not required.
 *
 *    2. Altered source versions must be plainly marked as such, and must not be
 *    misrepresented as being the original software.
 *
 *    3. This notice may not be removed or altered from any source
 *    distribution.
 */

package rsalesc.runner;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class DuelChallenge {
    public static final String DEFAULT_GROUP = "";


    public final String name;
    public final int rounds;
    public final ScoringStyle scoringStyle;
    public final int battleFieldWidth;
    public final int battleFieldHeight;
    public final List<BotListGroup> referenceBotGroups;
    public final List<String> allReferenceBots;

    public DuelChallenge(String name, int rounds, ScoringStyle scoringStyle,
                         int battleFieldWidth, int battleFieldHeight,
                         List<BotListGroup> referenceBotGroups) {
        this.name = name;
        this.rounds = rounds;
        this.scoringStyle = scoringStyle;
        this.battleFieldWidth = battleFieldWidth;
        this.battleFieldHeight = battleFieldHeight;
        this.referenceBotGroups = referenceBotGroups;
        this.allReferenceBots = getAllReferenceBots();
    }

    private List<String> getAllReferenceBots() {
        List<String> referenceBots = new ArrayList<>();
        for (BotListGroup group : referenceBotGroups) {
            referenceBots.addAll(group.referenceBots);
        }
        return referenceBots;
    }

    public boolean hasGroups() {
        return referenceBotGroups.size() > 1;
    }

    public static DuelChallenge load(String challengeFilePath) throws IOException {
        List<String> fileLines = Files.readAllLines(
                Paths.get(challengeFilePath), Charset.defaultCharset());

        String name = fileLines.get(0);
        ScoringStyle scoringStyle =
                ScoringStyle.parseStyle(fileLines.get(1).trim());
        int rounds = Integer.parseInt(
                fileLines.get(2).toLowerCase().replaceAll("rounds", "").trim());
        List<BotListGroup> botGroups = new ArrayList<>();
        List<String> groupBots = new ArrayList<>();
        String groupName = DEFAULT_GROUP;

        Integer width = null;
        Integer height = null;
        int maxBots = 1;
        for (int x = 3; x < fileLines.size(); x++) {
            String line = fileLines.get(x).trim();
            if (line.matches("^\\d+$")) {
                int value = Integer.parseInt(line);
                if (width == null) {
                    width = value;
                } else if (height == null) {
                    height = value;
                }
            } else if (line.length() > 0 && !line.contains("#")) {
                if (line.contains("{")) {
                    groupName = line.replace("{", "").trim();
                } else if (line.contains("}")) {
                    botGroups.add(new BotListGroup(groupName, groupBots));
                    groupName = DEFAULT_GROUP;
                    groupBots = new ArrayList<>();
                } else {
                    List<String> botList = new ArrayList<>(Arrays.asList(line.split(" *, *")));

                    botList.removeIf(new Predicate<String>() {
                        @Override
                        public boolean test(String botName) {
                            if (botName.contains(".") && botName.contains(" ")) {
                                return false;
                            } else {
                                System.out.println("WARNING: " + botName + " doesn't look "
                                        + "like a bot name, ignoring.");
                                return true;
                            }
                        }
                    });

                    maxBots = Math.max(maxBots, 1 + botList.size());
                    groupBots.addAll(botList);
                }
            }
        }

        if (maxBots > 2) {
            throw new RuntimeException("Duel challenge cant have more than "
                    + "2 bots in a battle.");
        }

        if (!groupBots.isEmpty()) {
            botGroups.add(new BotListGroup(groupName, groupBots));
        }

        if(width != null && width != 800 || height != null && height != 600)
            throw new RuntimeException("Duel challenge field must be 800x600");

        return new DuelChallenge(name, rounds, scoringStyle,
                (width == null ? 800 : width), (height == null ? 600 : height),
                botGroups);
    }

    public static class BotListGroup {
        public final String name;
        public final List<String> referenceBots;

        public BotListGroup(String name, List<String> referenceBots) {
            this.name = name;
            this.referenceBots = referenceBots;
        }

        public boolean isDefaultGroup() {
            return name.equals(DEFAULT_GROUP);
        }
    }

    public enum ScoringStyle {
        PERCENT_SCORE("Average Percent Score", false),
        SURVIVAL_FIRSTS("Survival Firsts", false),
        SURVIVAL_SCORE("Survival Score", false),
        BULLET_DAMAGE("Bullet Damage", true),
        MOVEMENT_CHALLENGE("Movement Challenge", true);

        private String _description;
        private boolean _isChallenge;

        private ScoringStyle(String description, boolean isChallenge) {
            _description = description;
            _isChallenge = isChallenge;
        }

        public static ScoringStyle parseStyle(String styleString) {
            if (styleString.contains("PERCENT_SCORE")) {
                return PERCENT_SCORE;
            } else if (styleString.contains("BULLET_DAMAGE")) {
                return BULLET_DAMAGE;
            } else if (styleString.contains("SURVIVAL_FIRSTS")) {
                return SURVIVAL_FIRSTS;
            } else if (styleString.contains("SURVIVAL_SCORE")) {
                return SURVIVAL_SCORE;
            } else if (styleString.contains("MOVEMENT_CHALLENGE")
                    || styleString.contains("ENERGY_CONSERVED")) {
                return MOVEMENT_CHALLENGE;
            } else {
                throw new IllegalArgumentException(
                        "Unrecognized scoring style: " + styleString);
            }
        }

        public String getDescription() {
            return _description;
        }

        public boolean isChallenge() {
            return _isChallenge;
        }
    }
}