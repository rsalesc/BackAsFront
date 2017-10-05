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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Roberto Sales on 05/10/17.
 */
public class BatchDuelRunner {
    private final String myself;
    private final RobocodeEngineProvider provider;
    private final List<Pair<String, Integer>> matches;
    private boolean layered;
    private int seasons;

    private int rounds = 35;

    public BatchDuelRunner(RobocodeEngineProvider provider, String myself, Pair<String, Integer> ...matches) {
        this.myself = myself;
        this.provider = provider;
        this.matches = Arrays.asList(matches);
        this.layered = false;
        this.seasons = 0;
    }

    public BatchDuelRunner(RobocodeEngineProvider provider, String myself, int seasons, String ...matches) {
        this.myself = myself;
        this.provider = provider;
        this.matches = new ArrayList<>();
        this.layered = true;
        this.seasons = seasons;

        for(String match : matches) {
            this.matches.add(new Pair<>(match, seasons));
        }
    }

    public BatchDuelResults run() {
        BatchDuelResults results = new BatchDuelResults();

        if(layered) {
            for(int cnt = 0; cnt < seasons; cnt++) {
                for (Pair<String, Integer> match : matches) {
                    if (match.second > cnt) {
                        System.out.print("\rRunning " + (cnt+1) + "-th match against " + match.first + "...");

                        DuelBattleRunner runner = new DuelBattleRunner(myself, match.first, rounds);
                        BatchDuelObserver observer = new BatchDuelObserver(myself);
                        runner.run(provider, observer);

                        if (observer.getDuelResult() == null)
                            throw new IllegalStateException();

                        results.add(observer.getDuelResult());
                        System.out.println("\r" + observer.getDuelResult());
                        BatchDuelResults filteredResults = new BatchDuelResults();
                        filteredResults.add(results.filterByEnemy(match.first));
                        System.out.println("\t\t" + filteredResults);
                    }
                }

                System.out.println("\nSeason #" + (cnt+1) + ": " + results + "\n");
            }

            System.out.println("\nDumping final results...");
            for(Pair<String, Integer> match : matches) {
                BatchDuelResults filteredResults = new BatchDuelResults();
                filteredResults.add(results.filterByEnemy(match.first));

                System.out.println(match.first + ": " + filteredResults);
            }

            System.out.println("General results: " + results);
        } else {
            throw new IllegalStateException();
        }

        return results;
    }

    public void setRounds(int rounds) {
        this.rounds = rounds;
    }

    public void setSeasons(int seasons) {
        this.layered = seasons > 0;
        this.seasons = seasons;
    }
}
