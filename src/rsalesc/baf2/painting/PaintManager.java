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

package rsalesc.baf2.painting;

import robocode.Condition;
import rsalesc.baf2.core.Component;
import rsalesc.baf2.core.KeyHandler;
import rsalesc.baf2.core.listeners.PaintListener;
import rsalesc.baf2.core.utils.geometry.Point;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Roberto Sales on 09/10/17.
 */
public class PaintManager extends Component implements PaintListener {
    private ArrayList<KeyPainting> paintings = new ArrayList<>();
    private Map<String, AWTKeyStroke> mappings = new TreeMap<>();

    private KeyHandler getKeyHandler() {
        return KeyHandler.getInstance();
    }

    private void addGroup(AWTKeyStroke stroke, String s) {
        if(mappings.containsKey(s) &&
                (mappings.get(s).getKeyCode() != stroke.getKeyCode() || mappings.get(s).getModifiers() != stroke.getModifiers()))
            throw new IllegalStateException("one group toggled by two different keys is not valid!");

        mappings.put(s, stroke);
    }

    public void add(Painting painting) {
        paintings.add(new KeyPainting(null, painting, true, null));
    }

    public void add(AWTKeyStroke stroke, String group, Painting painting, boolean state) {
        add(stroke, group, painting, state, null);
    }

    public void add(AWTKeyStroke stroke, String group, Painting painting) {
        add(stroke, group, painting, false, null);
    }

    public void add(AWTKeyStroke stroke, String group, Painting painting, Condition condition) {
        add(stroke, group, painting, false, condition);
    }

    public void add(AWTKeyStroke stroke, String group, Painting painting, boolean state, Condition condition) {
        addGroup(stroke, group);
        paintings.add(new KeyPainting(stroke, painting, state));
    }

    public void add(int keyCode, String group, Painting painting, boolean state, Condition condition) {
        AWTKeyStroke stroke = AWTKeyStroke.getAWTKeyStroke(keyCode, 0);
        add(stroke, group, painting, state, condition);
    }

    public void add(int keyCode, String group, Painting painting, boolean state) {
        add(keyCode, group, painting, state, null);
    }

    public void add(int keyCode, String group, Painting painting) {
        add(keyCode, group, painting, false, null);
    }

    public void add(int keyCode, String group, Painting painting, Condition condition) {
        add(keyCode, group, painting, false, condition);
    }

    @Override
    public void onPaint(Graphics2D gr) {
        KeyHandler handler = getKeyHandler();

        drawMenu(new G(gr));

        for(KeyPainting keyPainting : paintings) {
            if(keyPainting.enabled(handler))
                keyPainting.painting.paint(new G(gr));
        }
    }

    public void drawMenu(G g) {
        KeyHandler handler = getKeyHandler();
        FontMetrics metrics = g.getGraphics().getFontMetrics();

        StringBuilder builder = new StringBuilder();

        boolean first = true;
        for(Map.Entry<String, AWTKeyStroke> group : mappings.entrySet()) {
            if(!first) builder.append("  ");
            first = false;
            builder.append(getGroupString(group));
        }

        final double stringWidth = metrics.stringWidth(builder.toString());

        final double Y = getMediator().getBattleField().getHeight() - 15;
        double X = getMediator().getBattleField().getWidth() / 2 - stringWidth / 2;

        first = true;
        for(Map.Entry<String, AWTKeyStroke> group : mappings.entrySet()) {
            if(!first) {
                g.drawString(new Point(X, Y), "  ");
                X += metrics.stringWidth("  ");
            }

            first = false;
            g.drawString(new Point(X, Y), getGroupString(group), handler.enabled(group.getValue()) ? Color.ORANGE : Color.DARK_GRAY);
            X += metrics.stringWidth(getGroupString(group));
        }
    }

    public String getGroupString(Map.Entry<String, AWTKeyStroke> entry) {
        return entry.getKey() + " [" +
                KeyEvent.getKeyModifiersText(entry.getValue().getModifiers()) + " "  +
                KeyEvent.getKeyText(entry.getValue().getKeyCode()) + " ]";
    }

    private static class KeyPainting {
        public final AWTKeyStroke stroke;
        public final Painting painting;
        public final boolean defaultState;
        public final Condition condition;

        private KeyPainting(AWTKeyStroke stroke, Painting painting, boolean defaultState, Condition condition) {
            this.stroke = stroke;
            this.painting = painting;
            this.defaultState = defaultState;
            this.condition = condition;
        }

        private KeyPainting(AWTKeyStroke stroke, Painting painting, boolean defaultState) {
            this.stroke = stroke;
            this.painting = painting;
            this.defaultState = defaultState;
            this.condition = null;
        }

        public boolean enabled(KeyHandler handler) {
            return (condition == null || condition.test()) && (stroke == null || handler.enabled(stroke) != defaultState);
        }
    }
}
