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

package rsalesc.mega.learning;

import rsalesc.baf2.core.utils.Pair;
import rsalesc.mega.learning.genetic.*;
import rsalesc.mega.learning.recording.*;
import rsalesc.mega.utils.Strategy;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by Roberto Sales on 01/10/17.
 */

public class Tuning {
    private static final int THREADS = 5;

    public static void main(String[] args) throws IOException, NoSuchMethodException {
        final int iteration = 1;

        DuelRecorderRunner runner = new DuelRecorderRunner();
//        DuelRecordSuperPack adaptivePack = ensureAdaptive(runner, iteration);
        DuelRecordSuperPack randomPack = ensureRandom(runner);
        DuelRecordSuperPack sampledPack = ensureSampled(runner);

        System.out.println(evolveRandom(DuelRecordSuperPack.merge(randomPack, sampledPack)));

//        System.out.println(evolveAdaptive(adaptivePack, iteration));

        runner.getEngineProvider().close();

        System.exit(0);
    }

    public static Strategy evolveAdaptive(DuelRecordSuperPack pack, int iteration) throws IOException, NoSuchMethodException {
        File geneticCache = new File("generations/adaptive_pairings_" + iteration + ".dat");

        // make population bigger than the local search generated candidates to add some random factor to the algorithm
        GunBattleTrainer trainer = new GunBattleTrainer(10, pack,
                GeneticAdaptiveTargeting.class, new BaseAdaptiveStrategy(), THREADS);

        trainer.setCache(geneticCache);
        trainer.log();

        return trainer.train(20);
    }

    public static Strategy evolveRandom(DuelRecordSuperPack pack) throws IOException, NoSuchMethodException {
        File geneticCache = new File("generations/random_pairings.dat");

        // make population bigger than the local search generated candidates to add some random factor to the algorithm
        GunBattleTrainer trainer = new GunBattleTrainer(12, pack,
                GeneticRandomTargeting.class, new BaseRandomStrategy(), THREADS);

        trainer.setCache(geneticCache);
        trainer.log();

        return trainer.train(20);
    }

    public static DuelRecordSuperPack ensureAdaptive(DuelRecorderRunner runner, int iteration) {
        DuelRecordFS fs = new DuelRecordFS("records/normal/" + iteration, "rsalesc.mega.RecorderBot*");
        DuelRecordEnsurer ensurer = new DuelRecordEnsurer(runner, fs);

        ensurer.log();

        try {
            return ensurer.ensure(Arrays.asList(ADAPTIVE_PAIRINGS), 35, true);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static DuelRecordSuperPack ensureRandom(DuelRecorderRunner runner) {
        DuelRecordFS fs = new DuelRecordFS("records/tick", "rsalesc.mega.TickRecorderBot*");
        DuelRecordEnsurer ensurer = new DuelRecordEnsurer(runner, fs);

        ensurer.log();

        try {
            return ensurer.ensure(Arrays.asList(RANDOM_PAIRINGS), 35, true);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static DuelRecordSuperPack ensureSampled(DuelRecorderRunner runner) {
        DuelRecordFS fs = new DuelRecordFS("records/tick", "rsalesc.mega.TickRecorderBot*");
        DuelRecordEnsurer ensurer = new DuelRecordEnsurer(runner, fs);

        ensurer.log();

        try {
            return ensurer.ensure(Arrays.asList(SAMPLED_PAIRINGS), 35, true);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static final Pair<String, Integer>[] TEST_PAIRINGS = new Pair[]{
      new Pair<>("jk.nano.Machete 2.0", 2)
    };

    public static final Pair<String, Integer>[] ADAPTIVE_PAIRINGS = new Pair[]{
            new Pair<>("mue.Ascendant 1.2.27", 4),
            new Pair<>("pez.rumble.CassiusClay 2rho.02no", 4),
            new Pair<>("jk.mini.CunobelinDC 1.2", 4),
            new Pair<>("aaa.ScalarBot 0.012l29", 4),
            new Pair<>("pulsar.PulsarMax 0.8.9", 4),
            new Pair<>("florent.test.Toad 0.14t", 4),
            new Pair<>("tide.pear.Pear 0.62.1", 4),
            new Pair<>("simonton.mini.WeeksOnEnd 1.10.4", 4),
            new Pair<>("jam.RaikoMX 0.32", 4),
            new Pair<>("voidious.Diamond 1.8.22", 4),
            new Pair<>("abc.Shadow 3.83c", 4),
            new Pair<>("kc.serpent.WaveSerpent 2.11", 4),
            new Pair<>("kc.serpent.Hydra 0.21", 4),
            new Pair<>("cjm.chalk.Chalk 2.6.Be", 4),
            new Pair<>("wcsv.Engineer.Engineer 0.5.4", 4),
            new Pair<>("darkcanuck.Pris 0.92", 4),
            new Pair<>("cs.Nene 1.0.5", 4),
            new Pair<>("jk.mega.DrussGT 3.1.4", 4),
            new Pair<>("aw.Gilgalad 1.99.5c", 4),
            new Pair<>("davidalves.Phoenix 1.02", 4),
            new Pair<>("cs.s2.Seraphim 2.3.1", 4),
            new Pair<>("justin.DemonicRage 3.20", 4),
            new Pair<>("voidious.Dookious 1.573c", 4),
            new Pair<>("ags.Midboss 1q.fast", 4),
            new Pair<>("dsekercioglu.Rechner 1.02", 4),
            new Pair<>("tjk.deBroglie rev0108", 4),
            new Pair<>("cf.proto.Shiva 2.2", 4)
    };

    public static final Pair<String, Integer>[] RANDOM_PAIRINGS = new Pair[]{
            new Pair<>("jam.micro.RaikoMicro 1.44", 6),
            new Pair<>("gh.GrubbmGrb 1.2.4", 6),
            new Pair<>("davidalves.net.DuelistMicro 1.22", 6),
            new Pair<>("kawigi.sbf.FloodMini 1.4", 6),
            new Pair<>("abc.tron3.Tron 3.11", 6),
            new Pair<>("simonton.micro.WeeklongObsession 3.4.1", 6),
            new Pair<>("apv.Aspid 1.7", 6),
            new Pair<>("kawigi.mini.Fhqwhgads 1.1", 6),
            new Pair<>("nz.jdc.micro.HedgehogGF 1.5", 6),
            new Pair<>("nz.jdc.nano.PralDeGuerre 1.2", 6),
            new Pair<>("pe.SandboxDT 3.02", 6),
            new Pair<>("cx.mini.Cigaret 1.31", 6),
            new Pair<>("rdt.AgentSmith.AgentSmith 0.5", 5),
            new Pair<>("cjm.Charo 1.1", 5),
            new Pair<>("apv.LauLectrik 1.2", 6),
            new Pair<>("ary.SMG 1.01", 6),
            new Pair<>("mld.Moebius 2.9.3", 5),
            new Pair<>("bvh.mini.Freya 0.55", 5),
            new Pair<>("kawigi.sbf.FloodHT 0.9.2", 6),
    };

    public static final Pair<String, Integer>[] SAMPLED_PAIRINGS = new Pair[]{
            new Pair<>("ebo.Sparse 0.02", 6),
            new Pair<>("arthord.KostyaTszyu Beta2", 6),
            new Pair<>("strider.Festis 1.2.1", 6),
            new Pair<>("rz.GlowBlowAPM 1.0", 6),
            new Pair<>("alex.Diabolo5 1.1", 6),
            new Pair<>("muf.CrazyKitten 0.9", 6),
            new Pair<>("radnor.DoctorBob 1.42", 6),
            new Pair<>("mladjo.AIR 0.7", 6),
            new Pair<>("dummy.micro.HummingBird 2.14", 6),
            new Pair<>("jab.avk.ManuelGallegus 0.6", 6),
            new Pair<>("sheldor.nano.Epeeist 1.1.0", 6),
            new Pair<>("PkKillers.PkAssassin 1.0", 6),
            new Pair<>("eem.IWillFireNoBullet v2.4", 6),
            new Pair<>("sul.NanoR2 1.32", 6),
            new Pair<>("jwst.DAD.DarkAndDarker 1.1", 6),
            new Pair<>("cx.micro.Blur 0.2", 6),
            new Pair<>("davidalves.net.Duelist 0.1.6src", 6),
            new Pair<>("DM.Mijit .3", 6),
            new Pair<>("buba.Archivist 0.1", 6),
            new Pair<>("ntc.Plains 0.9", 6),
            new Pair<>("jaw.Mouse 0.11", 6),
            new Pair<>("sheldor.nano.PointInLine 1.0", 6),
            new Pair<>("simonton.micro.GFMicro 1.0", 6),
            new Pair<>("pez.mini.VertiLeach 0.4.0", 6),
            new Pair<>("rz.Artist 0.2", 6),
            new Pair<>("lorneswork.Predator 1.0", 6),
            new Pair<>("ags.Glacier 0.2.7", 6),
            new Pair<>("blir.nano.Cabbage R1.0.1", 6),
            new Pair<>("timmit.TimmiT 0.22", 6),
            new Pair<>("apv.TheBrainPi 0.5fix", 6),
            new Pair<>("barontrozo.BaronTrozo 1.7.6", 6),
            new Pair<>("mz.AdeptBSB 1.03", 6),
            new Pair<>("mk.Alpha 0.2.1", 6),
            new Pair<>("serenity.moonlightBat 1.17", 6),
            new Pair<>("staticline.whiskey.Whiskey 0.6", 6),
            new Pair<>("cw.megas.GhostShell GT", 6),
            new Pair<>("mb.Beast 0.4.1", 6),
            new Pair<>("davidalves.net.DuelistMini 1.1", 6),
            new Pair<>("cx.nano.Smog 2.6", 6),
            new Pair<>("cx.BlestPain 1.41", 6),
            new Pair<>("nz.jdc.nano.NeophytePattern 1.1", 6),
            new Pair<>("deith.Czolgzilla 0.11", 6),
            new Pair<>("suh.nano.OscillatorL 1.00", 6),
            new Pair<>("dsx724.VSAB_EP3a 1.0", 6),
            new Pair<>("non.mega.NoName 0.0", 6),
            new Pair<>("oog.micro.MagicD3 0.41", 6),
            new Pair<>("dmp.micro.Aurora 1.41", 6),
            new Pair<>("mz.Adept 2.65", 6),
            new Pair<>("dft.Virgin 1.25", 6),
            new Pair<>("Legend.X_FireFly 1.3", 6),
            new Pair<>("sul.Bicephal 1.2", 6),
            new Pair<>("nat.nano.Ocnirp 1.73", 6),
            new Pair<>("kawigi.sbf.FloodMicro 1.5", 6),
            new Pair<>("klein.GottesKrieger 1.1", 6),
            new Pair<>("hlavko.micro.Flex 1.5", 6),
            new Pair<>("step.nanoPri 1.0", 6),
            new Pair<>("stuff.Vlad 0.1", 6),
            new Pair<>("lion.Kresnanano 1.0", 6),
            new Pair<>("shinh.Entangled 0.3", 6),
            new Pair<>("oog.nano.SavantVS 1.1", 6),
            new Pair<>("drm.Magazine 0.39", 6),
            new Pair<>("djc.Aardvark 0.3.6", 6),
            new Pair<>("rfj.Sunburn 1.1", 6),
            new Pair<>("mladjo.GnuKlub 0.1", 6),
            new Pair<>("suh.nano.MirrorL 1.00", 6),
            new Pair<>("nz.jdc.nano.AralR 1.1", 6),
            new Pair<>("Krabb.fe4r.Fe4r 0.4", 6),
            new Pair<>("scheronimus.NanoScheroBot 1.0", 6),
            new Pair<>("tobe.Saturn lambda", 6),
            new Pair<>("stelo.MatchupMicro 1.2", 6),
            new Pair<>("timmit.nano.TimDog 0.33", 6),
            new Pair<>("dragonbyte.Neutrino 4", 8),
            new Pair<>("ne.Chimera 1.2", 6),
            new Pair<>("urdos.URDOS 1.3", 6),
            new Pair<>("ntc.Cannon 1.12test", 6),
            new Pair<>("dz.GalbaMicro 0.11", 6),
            new Pair<>("agrach.Dalek 1.0", 6),
            new Pair<>("myl.nano.Kakuru 1.20", 6),
            new Pair<>("nz.jdc.nano.NeophyteSRAL 1.3", 6),
            new Pair<>("maribo.Omicron 1.0", 6),
            new Pair<>("pez.clean.Swiffer 0.2.9", 6),
            new Pair<>("doka.Shinigami 2.2", 6),
            new Pair<>("jekl.mini.BlackPearl .91", 6),
            new Pair<>("lazarecki.mega.PinkerStinker 0.7", 6),
            new Pair<>("pa3k.Manta 1.20", 6),
            new Pair<>("EH.Fusion 0.32", 6),
            new Pair<>("mnt.SurferBot 0.2.5", 6),
            new Pair<>("suh.nano.RandomPM 1.02", 6),
            new Pair<>("dsekercioglu.Tomahawk 5.04x", 6),
            new Pair<>("banshee.micro.Nexus6 0.3.0", 6),
            new Pair<>("mue.Hyperion 0.8", 6),
            new Pair<>("tvv.nano.Polaris 1.2", 6),
            new Pair<>("davidalves.net.DuelistNano 1.0", 6),
            new Pair<>("casey.Flump 1.0", 6),
            new Pair<>("pa3k.Quark 1.02", 6),
            new Pair<>("Legend.Biogon 1.5", 6),
            new Pair<>("m3thos.Eva02 0.7.1", 6),
            new Pair<>("robar.nano.BlackWidow 1.3", 6),
            new Pair<>("vic.Locke 0.7.5.5", 6),
            new Pair<>("md.Pasta 1.1", 6),
            new Pair<>("stelo.MirrorMicro 1.1", 8),
            new Pair<>("pez.micro.Aristocles 0.3.7", 6),
            new Pair<>("nat.nano.OcnirpSNG 1.0b", 6),
            new Pair<>("morbid.MorbidPriest 1.0", 6),
            new Pair<>("ethdsy.Malacka 2.4", 6),
            new Pair<>("tobe.Fusion 1.0", 6),
            new Pair<>("ary.Crisis 1.0", 6),
            new Pair<>("ghent.ArthurPanzergon 1.0.0", 6),
            new Pair<>("chase.pm.Pytko 1.0", 6),
            new Pair<>("axeBots.Musashi 2.18", 6),
            new Pair<>("paulk.PaulV3 1.7", 6),
            new Pair<>("lrem.Spectre 0.4.4", 6),
            new Pair<>("wcsv.Stampede 1.3.3", 6),
            new Pair<>("ags.micro.Carpet 1.1", 6),
            new Pair<>("oog.nano.MagicD2 2.4", 6),
            new Pair<>("com.cohesiva.robocode.ManOwaR 1.0", 6),
            new Pair<>("penguin.MrFreeze 1.0a", 6),
            new Pair<>("et.Predator 1.8", 6),
            new Pair<>("sgp.SleepingGoat 1.1", 6),
            new Pair<>("squidM.SquidmanNano 1.0", 6),
            new Pair<>("robar.micro.Topaz 0.25", 6),
            new Pair<>("pe.mini.SandboxMini 1.2", 6),
            new Pair<>("com.arsenic.NewTest 1.0", 6),
            new Pair<>("jaara.LambdaBot 1.1", 6),
            new Pair<>("oog.nano.Caligula 1.15", 6),
            new Pair<>("md.November 1.0", 6),
            new Pair<>("dummy.mini.Parakeet 2.40", 6),
            new Pair<>("bumblebee.Bumblebee 1.0", 6),
            new Pair<>("tm.Yuugao 1.0", 6),
            new Pair<>("mz.NanoDeath 2.56", 6),
            new Pair<>("metal.small.dna2.MCoolDNA 1.5", 6),
            new Pair<>("kinsen.nano.Hoplomachy 1.6", 6),
            new Pair<>("robar.nano.Vespa 0.95", 6),
            new Pair<>("metal.small.MCool 1.21", 6),
            new Pair<>("vjik.UnViolation 1.1", 6),
            new Pair<>("arthord.micro.Apoptygma 0.4", 6),
            new Pair<>("robar.micro.Gladius 1.15", 6),
            new Pair<>("kinsen.melee.Angsaichmophobia 1.8c", 6),
            new Pair<>("trab.nano.AinippeNano 1.3", 6),
            new Pair<>("supersample.SuperSpinBot 1.0", 6),
            new Pair<>("robar.micro.Kirbyi 1.0", 6),
            new Pair<>("cjm.Che 1.2", 6),
            new Pair<>("cx.CigaretBH 1.03", 6),
            new Pair<>("amk.ChumbaWumba 0.3", 6),
            new Pair<>("kawigi.nano.ThnikkaBot 0.9", 6),
            new Pair<>("marksteam.Phoenix 1.0", 6),
            new Pair<>("nat.micro.Reepicheep 0.1a", 6),
            new Pair<>("satan.R0 0.2", 6),
            new Pair<>("jrm.Test0 1.0", 6),
            new Pair<>("rz.SmallDevil 1.502", 6),
            new Pair<>("cx.mini.Nimrod 0.55", 6),
            new Pair<>("pedersen.Hubris 2.4", 6),
            new Pair<>("mz.Movement 1.8", 6)
    };
}
