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

/**
 * <h2>{@link ItemRenderer}<br>
 * <sub>[in short] (TODO).</sub></h2>
 *
 * <p>
 * [description / usage].
 * </p>
 *
 * <p>
 * <i>Feb 20, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
public class ItemRenderer<I> {

    protected I item;


    /**
     * Create a new {@link ItemRenderer} instance.
     */
    public ItemRenderer(I item) {

        this.item = item;
    }

    /**
     * @return The item of this {@link ItemRenderer}.
     */
    public I getItem() {

        return this.item;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        return this.item.toString();
    }
}
