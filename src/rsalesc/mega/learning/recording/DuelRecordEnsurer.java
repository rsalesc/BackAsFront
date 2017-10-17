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

package rsalesc.mega.learning.recording;

import rsalesc.baf2.core.utils.Pair;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.file.ClosedFileSystemException;
import java.nio.file.FileSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Created by Roberto Sales on 01/10/17.
 */
public class DuelRecordEnsurer {
    private static final int MAX_RETRIES = 3;

    private final DuelRecorderRunner runner;
    private final DuelRecordFS fs;

    private boolean logs = false;

    public DuelRecordEnsurer(DuelRecorderRunner runner, DuelRecordFS fs) {
        this.runner = runner;
        this.fs = fs;
    }

    public void log() {
        logs = true;
    }

    private DuelRecordPackage loadPack(String enemyName, int rounds, boolean enforceRounds) throws IOException {
        DuelRecordPackage pack;
        if(enforceRounds)
            pack = fs.load(enemyName, duelRecord -> duelRecord.getRounds() == rounds);
        else
            pack = fs.load(enemyName);

        return pack;
    }


    public DuelRecordPackage ensure(int n, String enemyName, int rounds, boolean enforceRounds) throws IOException {
        DuelRecordPackage pack = loadPack(enemyName, rounds, enforceRounds);

        int goodRecords = pack.count();
        int beforeGoodRecords = -1;
        int retries = 0;

        while(goodRecords < n) {
            String verb = beforeGoodRecords == goodRecords
                    ? "Retry no. " + ++retries + " of"
                    : "Ensuring" + ((retries = 0) == 0 ? "" : "");

            if(retries > MAX_RETRIES) {
                if(logs)
                    System.out.println("[" + fs.getMyself() +
                            " vs " + enemyName + "] exceeded the max. number of retries");
                return null;
            }

            if(logs)
                System.out.println(verb + " [" + fs.getMyself() +
                        " vs " + enemyName + "] " + (goodRecords + 1) + "-th battle (of " + n + ")");

            beforeGoodRecords = goodRecords;

            try {
                DuelRecord record = runner.run(fs.getMyself(), enemyName, rounds);

                try {
                    pack.save(record);
                }  catch (ClosedChannelException | ClosedFileSystemException exception) {
                    // ZipFileSystem somehow was closed (or had it's channel closed)
                    // try to re-mount it and re-save the record as a last resort

                    try {
                        pack.close();
                    } catch (Exception ignored) {}

                    pack = loadPack(enemyName, rounds, enforceRounds);
                    pack.save(record);
                }

                goodRecords++;
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.gc();
        }

        if(logs) {
            if(enforceRounds)
                System.out.println("Finished ensuring " + n + " battles of " + rounds + " rounds for " + enemyName + ".");
            else
                System.out.println("Finished ensuring " + n + " battles for " + enemyName + ".");
        }

        return pack;
    }

    public DuelRecordSuperPack ensure(List<Pair<String, Integer>> ensurances, int rounds, boolean enforceRounds)
            throws IOException {
        ArrayList<DuelRecordPackage> packs = new ArrayList<>();

        int totalBattles = 0;

        for(Pair<String, Integer> ensurance : ensurances) {
            totalBattles += ensurance.second;
        }

        if(logs)
            System.out.println("Ensuring a total of " + totalBattles + " battles of " + rounds + " rounds.");

        boolean ensured = true;

        for(Pair<String, Integer> ensurance : ensurances) {
            DuelRecordPackage pack = ensure(ensurance.second, ensurance.first, rounds, enforceRounds);
            if(pack == null) {
                // that means this bot has skipped MAX_RETRIES retries
                ensured = false;
            } else if (ensured) {
                packs.add(pack);
            }
        }

        if(logs && !ensured)
            System.out.println("Not all battles could be recorded, check for some error and re-run the recorder.");

        return ensured ? new DuelRecordSuperPack(packs) : null;
    }
}
