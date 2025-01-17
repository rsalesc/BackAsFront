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

package rsalesc.mega.tracking;

import rsalesc.baf2.core.Component;
import rsalesc.baf2.core.utils.PredictedHashMap;
import rsalesc.baf2.tracking.EnemyRobot;
import rsalesc.baf2.tracking.EnemyTracker;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by Roberto Sales on 12/09/17.
 */
public class MovieTracker extends Component {
    private long sequenceSize;
    private long minimumSize;
    private long allowedGap;

    private HashMap<String, LinkedList<EnemyMovie>> pending = new PredictedHashMap<>(15);
    private HashMap<String, EnemyRobot> lastLeadActor = new PredictedHashMap<>(15);

    public MovieTracker(long sequenceSize, long minimumSize, long allowedGap) {
        this.sequenceSize = sequenceSize;
        this.allowedGap = allowedGap;
        this.minimumSize = minimumSize;
    }

    private EnemyRobot getLastLeadActor(EnemyRobot enemy) {
        return lastLeadActor.get(enemy.getName());
    }

    @Override
    public void beforeRun() {
//        super.beforeRun();

        long time = getMediator().getTime();
        EnemyRobot[] enemies = EnemyTracker.getInstance().getLatest();

        for (EnemyRobot enemy : enemies) {
            // remove sequences such that new scan breaks gap OR sequence size is exceeded
            LinkedList<EnemyMovie> list = pending.get(enemy.getName());

            if(list == null)
                pending.put(enemy.getName(), list = new LinkedList<>());

            Iterator<EnemyMovie> it = list.iterator();

            while(it.hasNext()) {
                EnemyMovie movie = it.next();
                EnemyRobot latest = movie.getLatest();

                if(enemy.getTime() - latest.getTime() > allowedGap || enemy.getTime() - movie.getLeadActor().getTime() > sequenceSize)
                    it.remove();
                else if(enemy.getTime() != latest.getTime())
                    movie.append(enemy);
            }

            // try to create a new minimum size sequence from scratch if it is enough to get a different lead actor
            EnemyRobot[] sequence = EnemyTracker.getInstance().getLog(enemy).getSequence(time - minimumSize);
            boolean bad = sequence.length == 0 || time - sequence[sequence.length - 1].getTime() > allowedGap
                    || getLastLeadActor(enemy) != null && getLastLeadActor(enemy).getTime() == sequence[0].getTime();

            for (int i = 1; i < sequence.length; i++) {
                bad = bad || sequence[i].getTime() - sequence[i - 1].getTime() > allowedGap;
            }

            if (!bad) {
                lastLeadActor.put(enemy.getName(), sequence[0]);

                EnemyMovie newMovie = new EnemyMovie(sequence);
                list.add(newMovie);

                for (Object obj : getListeners()) {
                    if (obj instanceof MovieListener) {
                        MovieListener listener = (MovieListener) obj;
                        listener.onNewMovie(newMovie);
                    }
                }
            }
        }
    }
}
