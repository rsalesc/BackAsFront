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

import rsalesc.baf2.core.utils.Pair;

import java.util.*;

/**
 * Created by Roberto Sales on 05/10/17.
 */
public class BatchDuelRunner {
    private final String myself;
    private final RobocodeEngineProvider provider;
    private final List<Match> matches;
    private int seasons = 1;

    private int rounds = 35;

    public BatchDuelRunner(RobocodeEngineProvider provider, String myself, int seasons, String ...matches) {
        this.myself = myself;
        this.provider = provider;
        this.matches = new ArrayList<>();
        this.seasons = seasons;

        for(String match : matches) {
            this.matches.add(new Match(match, null));
        }
    }

    public BatchDuelRunner(RobocodeEngineProvider provider, String myself, int seasons, DuelChallenge challenge) {
        this.rounds = challenge.rounds;
        this.matches = new ArrayList<>();
        this.seasons = seasons;
        this.myself = myself;
        this.provider = provider;

        for(DuelChallenge.BotListGroup group : challenge.referenceBotGroups) {
            String groupName = group.name;
            if(group.isDefaultGroup()) {
                groupName = null;
            }

            for(String bot : group.referenceBots) {
                this.matches.add(new Match(bot, groupName));
            }
        }
    }

    public BatchDuelResults run() {
        BatchDuelResults results = new BatchDuelResults();
        BatchDuelResults groupResults = new BatchDuelResults();
        HashMap<String, BatchDuelResults> groupedResults = new HashMap<>();

        for(Match match : matches) {
            if(!groupedResults.containsKey(match.group)) {
                BatchDuelResults nresult = new BatchDuelResults();
                groupedResults.put(match.group, nresult);
                groupResults.add(nresult);
            }
        }

        for(int cnt = 0; cnt < seasons; cnt++) {
            Iterator<Match> it = matches.iterator();
            while(it.hasNext()) {
                Match match = it.next();

                System.out.print("\rRunning " + (cnt+1) + "-th match against " + match.bot + "...");

                DuelBattleRunner runner = new DuelBattleRunner(myself, match.bot, rounds);
                BatchDuelObserver observer = new BatchDuelObserver(myself);
                runner.run(provider, observer);

                if (observer.getDuelResult() == null) {
                    System.err.println("\r" + "Could not reproduce battle against " + match.bot + ", removing it from list of reference bots.");
                    it.remove();
                    continue;
                }

                DuelResult result = observer.getDuelResult();
                results.add(result);
                groupedResults.get(match.group).add(result);

                // current battle result
                System.out.println("\r" + result);

                // current bot result
                BatchDuelResults filteredResults = new BatchDuelResults();
                filteredResults.add(results.filterByEnemy(match.bot));
                System.out.println("\t\t" + filteredResults);

                // current group result, if not null
                if(match.group != null) {
                    BatchDuelResults currentGroupResults = groupedResults.get(match.group);
                    System.out.println(match.group + ": " + currentGroupResults);
                }

                System.out.println();
            }

            System.out.println("\nSeason #" + (cnt+1) + ": " + results + "\n");
        }

        System.out.println("\nDumping final results...");
        dumpResults(results, groupedResults, true);

        return results;
    }

    public void dumpResults(BatchDuelResults results, HashMap<String, BatchDuelResults> groupedResults, boolean dumpIndividual) {
        HashMap<String, List<Pair<String, BatchDuelResults>>> partialResults = new HashMap<>();

        for(Match match : matches) {
            BatchDuelResults filteredResults = new BatchDuelResults();
            filteredResults.add(results.filterByEnemy(match.bot));

            if(!partialResults.containsKey(match.group))
                partialResults.put(match.group, new ArrayList<>());

            partialResults.get(match.group).add(new Pair<>(match.bot, filteredResults));
        }

        for(Map.Entry<String, List<Pair<String, BatchDuelResults>>> entry : partialResults.entrySet()) {
            if(groupedResults.size() > 1)
                System.out.println(getGroupName(entry.getKey()) + ": "  + groupedResults.get(entry.getKey()));

            if(dumpIndividual) {
                for (Pair<String, BatchDuelResults> pair : entry.getValue()) {
                    System.out.println("\t" + pair.first + ": " + pair.second);
                }
            }
        }

        System.out.println("General results: " + results);
    }

    public String getGroupName(String name) {
        if(name == null)
            return "Ungrouped";
        return name;
    }

    public void setRounds(int rounds) {
        this.rounds = rounds;
    }

    public void setSeasons(int seasons) {
        this.seasons = seasons;
    }

    private class Match {
        public final String bot;
        public final String group;

        private Match(String bot, String group) {
            this.bot = bot;
            this.group = group;
        }
    }
}
