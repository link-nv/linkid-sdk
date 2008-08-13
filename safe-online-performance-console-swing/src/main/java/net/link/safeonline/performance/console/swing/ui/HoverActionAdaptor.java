/*
 *   Copyright 2008, Maarten Billemont
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package net.link.safeonline.performance.console.swing.ui;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.Border;


/**
 * <h2>{@link HoverActionAdaptor}<br>
 * <sub>A mouse adaptor that invokes an action when one of its components is clicked.</sub></h2>
 * 
 * <p>
 * You are to implement {@link #clicked(JComponent)} yourself to provide the actions that must be undertaken whenever a
 * managed component is clicked.<br>
 * <br>
 * This adaptor will take care of showing a nice border around the managed components whenever they are hovered over and
 * hide this border whilst they are not.
 * </p>
 * 
 * <p>
 * <i>Feb 21, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
public abstract class HoverActionAdaptor extends MouseAdapter {

    private static final int         TOP          = 0, LEFT = 0, BOTTOM = 1, RIGHT = 0;

    private static final Border      EMPTY_BORDER = BorderFactory.createEmptyBorder(TOP, LEFT, BOTTOM, RIGHT);
    private static final Border      HOVER_BORDER = BorderFactory.createMatteBorder(TOP, LEFT, BOTTOM, RIGHT,
                                                          Color.lightGray);
    private static final Border      PRESS_BORDER = BorderFactory.createMatteBorder(TOP, LEFT, BOTTOM, RIGHT,
                                                          Color.black);
    private Map<JComponent, Boolean> managedComponents;


    /**
     * Registers this adaptor with the given components as a mouse listener and hides their borders.
     */
    public HoverActionAdaptor(JComponent... components) {

        this.managedComponents = new HashMap<JComponent, Boolean>();
        manage(components);
    }

    /**
     * Manage the given components.
     */
    public void manage(JComponent... components) {

        for (JComponent component : components) {
            this.managedComponents.put(component, true);

            component.addMouseListener(this);
            component.setBorder(EMPTY_BORDER);
        }
    }

    /**
     * Stop managing the given components.
     */
    public void unmanage(JComponent... components) {

        for (JComponent component : components) {
            this.managedComponents.remove(component);

            component.removeMouseListener(this);
            component.setBorder(EMPTY_BORDER);
        }
    }

    /**
     * Change whether given components should have their management enabled or disabled.
     */
    public void enable(boolean enabled, JComponent... components) {

        for (JComponent component : components) {
            if (!this.managedComponents.containsKey(component))
                throw new IllegalArgumentException("Given component is not managed!");

            this.managedComponents.put(component, enabled);

            if (!enabled)
                component.setBorder(EMPTY_BORDER);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseEntered(MouseEvent e) {

        if (this.managedComponents.containsKey(e.getSource()) && this.managedComponents.get(e.getSource()))
            getComponent(e).setBorder(HOVER_BORDER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseExited(MouseEvent e) {

        if (this.managedComponents.containsKey(e.getSource()) && this.managedComponents.get(e.getSource()))
            getComponent(e).setBorder(EMPTY_BORDER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mousePressed(MouseEvent e) {

        if (this.managedComponents.containsKey(e.getSource()) && this.managedComponents.get(e.getSource()))
            getComponent(e).setBorder(PRESS_BORDER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseReleased(MouseEvent e) {

        if (this.managedComponents.containsKey(e.getSource()) && this.managedComponents.get(e.getSource()))
            if (getComponent(e).contains(e.getPoint()))
                mouseEntered(e);
            else
                mouseExited(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseClicked(MouseEvent e) {

        if (this.managedComponents.containsKey(e.getSource()) && this.managedComponents.get(e.getSource()))
            clicked(getComponent(e));
    }

    private JComponent getComponent(MouseEvent e) {

        if (e.getSource() instanceof JComponent)
            return (JComponent) e.getSource();

        return null;
    }

    /**
     * This method gets called whenever one of the managed components is clicked.
     */
    protected abstract void clicked(JComponent component);
}
