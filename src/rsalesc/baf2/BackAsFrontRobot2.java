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

package rsalesc.baf2;

import robocode.*;
import rsalesc.baf2.core.Component;
import rsalesc.baf2.core.RobotMediator;
import rsalesc.baf2.core.listeners.*;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Roberto Sales on 22/07/17.
 */
public abstract class BackAsFrontRobot2 extends OldBackAsFrontRobot {
    private ArrayList<Component> components;
    private RobotMediator mediator;

    public abstract void initialize();

    public void add(Component component) {
        components.add(component);
    }

    public void addListener(Component component) {
        component.setVirtual(true);
        components.add(component);
    }

    protected void build() {
        mediator = new RobotMediator(this);
        for (Component component : components) {
            component.init(mediator);
        }
    }

    public ArrayList<Component> getComponents() {
        ArrayList<Component> res = new ArrayList<>();
        res.addAll(components);

        return res;
    }

    @Override
    public void run() {
        while (true) {
            for (Component component : components) {
                if (!component.isVirtual()) component.beforeRun();
            }

            for (Component component : components) {
                if (!component.isVirtual()) component.run();
            }

            for (Component component : components) {
                if (!component.isVirtual()) component.afterRun();
            }

            execute();
        }
    }

    public void handleEvents() {
        for (Component component : components) {
            if (component instanceof RobotDeathListener) {
                RobotDeathListener listener = (RobotDeathListener) component;
                for (RobotDeathEvent e : getRobotDeathEvents()) {
                    listener.onRobotDeath(e);
                }
            }
        }

        for (Component component : components) {
            if (component instanceof BulletListener) {
                BulletListener listener = (BulletListener) component;
                for (BulletMissedEvent e : getBulletMissedEvents()) {
                    listener.onBulletMissed(e);
                }
            }
        }

        for (Component component : components) {
            if (component instanceof BulletListener) {
                BulletListener listener = (BulletListener) component;
                for (BulletHitBulletEvent e : getBulletHitBulletEvents()) {
                    listener.onBulletHitBullet(e);
                }
            }
        }

        for (Component component : components) {
            if (component instanceof BulletListener) {
                BulletListener listener = (BulletListener) component;
                for (BulletHitEvent e : getBulletHitEvents()) {
                    listener.onBulletHit(e);
                }
            }
        }

        for (Component component : components) {
            if (component instanceof HitListener) {
                HitListener listener = (HitListener) component;
                for (HitByBulletEvent e : getHitByBulletEvents()) {
                    listener.onHitByBullet(e);
                }
            }
        }

        for (Component component : components) {
            if (component instanceof HitListener) {
                HitListener listener = (HitListener) component;
                for (HitWallEvent e : getHitWallEvents()) {
                    listener.onHitWall(e);
                }
            }
        }

        for (Component component : components) {
            if (component instanceof HitListener) {
                HitListener listener = (HitListener) component;
                for (HitRobotEvent e : getHitRobotEvents()) {
                    listener.onHitRobot(e);
                }
            }
        }

        for (Component component : components) {
            if (component instanceof ScannedRobotListener) {
                ScannedRobotListener listener = (ScannedRobotListener) component;
                for (ScannedRobotEvent e : getScannedRobotEvents()) {
                    listener.onScannedRobot(e);
                }
            }
        }
    }

    @Override
    public void onStatus(StatusEvent e) {
        super.onStatus(e);

        if (e.getTime() == 0) {
            dissociate();
            components = new ArrayList<>();
            initialize();
            build();
        }

        if (e.getTime() == 0 && e.getStatus().getRoundNum() == 0)
            _onBattleStarted();

        if (e.getTime() == 0)
            _onRoundStarted(e.getStatus().getRoundNum());

        for (Component component : components) {
            if (component instanceof StatusListener) {
                StatusListener listener = (StatusListener) component;
                listener.onStatus(e);
            }
        }

        for (Component component : components) {
            if (component instanceof TickListener) {
                TickListener listener = (TickListener) component;
                listener.onTick(e.getTime());
            }
        }
    }

    public void _onBattleStarted() {
        for (Component component : components) {
            if (component instanceof BattleStartedListener) {
                BattleStartedListener listener = (BattleStartedListener) component;
                listener.onBattleStarted();
            }
        }
    }

    public void _onRoundStarted(int round) {
        addCustomEvent(new Condition() {
            @Override
            public boolean test() {
                return true;
            }
        });

        for (Component component : components) {
            if (component instanceof RoundStartedListener) {
                RoundStartedListener listener = (RoundStartedListener) component;
                listener.onRoundStarted(round);
            }
        }
    }

    public void onCustomEvent(CustomEvent e) {
        handleEvents();
    }

    public void _onBulletHit(BulletHitEvent event) {
        super.onBulletHit(event);
        for (Component component : components) {
            if (component instanceof BulletListener) {
                BulletListener listener = (BulletListener) component;
                listener.onBulletHit(event);
            }
        }
    }

    public void _onBulletHitBullet(BulletHitBulletEvent event) {
        super.onBulletHitBullet(event);
        for (Component component : components) {
            if (component instanceof BulletListener) {
                BulletListener listener = (BulletListener) component;
                listener.onBulletHitBullet(event);
            }
        }
    }

    public void _onBulletMissed(BulletMissedEvent event) {
        super.onBulletMissed(event);
        for (Component component : components) {
            if (component instanceof BulletListener) {
                BulletListener listener = (BulletListener) component;
                listener.onBulletMissed(event);
            }
        }
    }

    public void _onHitByBullet(HitByBulletEvent event) {
        super.onHitByBullet(event);
        for (Component component : components) {
            if (component instanceof HitListener) {
                HitListener listener = (HitListener) component;
                listener.onHitByBullet(event);
            }
        }
    }

    public void _onHitRobot(HitRobotEvent event) {
        super.onHitRobot(event);
        for (Component component : components) {
            if (component instanceof HitListener) {
                HitListener listener = (HitListener) component;
                listener.onHitRobot(event);
            }
        }
    }

    public void _onHitWall(HitWallEvent event) {
        super.onHitWall(event);
        for (Component component : components) {
            if (component instanceof HitListener) {
                HitListener listener = (HitListener) component;
                listener.onHitWall(event);
            }
        }
    }

    public void _onRobotDeath(RobotDeathEvent event) {
        super.onRobotDeath(event);
        for (Component component : components) {
            if (component instanceof RobotDeathListener) {
                RobotDeathListener listener = (RobotDeathListener) component;
                listener.onRobotDeath(event);
            }
        }
    }

    public void _onScannedRobot(ScannedRobotEvent event) {
        super.onScannedRobot(event);
        for (Component component : components) {
            if (component instanceof ScannedRobotListener) {
                ScannedRobotListener listener = (ScannedRobotListener) component;
                listener.onScannedRobot(event);
            }
        }
    }

    @Override
    public void onWin(WinEvent event) {
        super.onWin(event);
        for (Component component : components) {
            if (component instanceof WinListener) {
                WinListener listener = (WinListener) component;
                listener.onWin(event);
            }
        }
    }

    @Override
    public void onRoundEnded(RoundEndedEvent event) {
        super.onRoundEnded(event);
        for (Component component : components) {
            if (component instanceof RoundEndedListener) {
                RoundEndedListener listener = (RoundEndedListener) component;
                listener.onRoundEnded(event);
            }
        }
    }

    @Override
    public void onBattleEnded(BattleEndedEvent event) {
        super.onBattleEnded(event);
        for (Component component : components) {
            if (component instanceof BattleEndedListener) {
                BattleEndedListener listener = (BattleEndedListener) component;
                listener.onBattleEnded(event);
            }
        }
    }

    @Override
    public void onPaint(Graphics2D g) {
        super.onPaint(g);
        for (Component component : components) {
            if (component instanceof PaintListener) {
                PaintListener listener = (PaintListener) component;
                listener.onPaint(g);
            }
        }
    }

    @Override
    public void setFire(double power) {
        setFireBullet(power);
    }

    @Override
    public Bullet setFireBullet(double power) {
        Bullet bullet = super.setFireBullet(power);
        if (bullet != null)
            _onFire(new FireEvent(getPoint(), bullet, getTime()));
        return bullet;
    }

    public void _onFire(FireEvent e) {
        for (Component component : components) {
            if (component instanceof FireListener) {
                FireListener listener = (FireListener) component;
                listener.onFire(e);
            }
        }
    }
}
