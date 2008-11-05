/*
 *   Copyright 2007, Maarten Billemont
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
package test.spike.net.link.safeonline;

import org.junit.Test;


/**
 * <h2>{@link TinyTests} - [in short] (TODO).</h2>
 * <p>
 * [description / usage].
 * </p>
 * <p>
 * <i>Dec 19, 2007</i>
 * </p>
 * 
 * @author mbillemo
 */
public class TinyTests {

    private static final int    DARKER_OFFSET = 17;
    private static final double DARKER_FACTOR = 1.26;


    @Test
    public void testColor() {

        Integer base = Integer.decode("#5A7500");

        System.out.println(getThemedColor(base, DARKER_FACTOR, DARKER_OFFSET));
    }

    private String getThemedColor(Integer base, double factor, int offset) {

        System.err.format("%X\n", ((base >> 16) % (0xFF + 1)));
        System.err.format("%X\n", (int) ((base >> 16) % (0xFF + 1) * factor));
        System.err.format("%X\n", (int) ((base >> 16) % (0xFF + 1) * factor + offset));
        int red = (int) ((base >> 16) % (0xFF + 1) * factor + offset);
        int green = (int) ((base >> 8) % (0xFF + 1) * factor + offset);
        int blue = (int) ((base >> 0) % (0xFF + 1) * factor + offset);

        return String.format("#%02X%02X%02X", red, green, blue);
    }

    void printBits(int number) {

        int i = 0;
        for (int left = number; left > 0;) {
            System.out.print(left % 2);
            left = left >> 1;
            i++;
            if (i % 8 == 0) {
                System.out.print(", ");
            }
        }
        System.out.println();
    }

    void print(Object[] array) {

        boolean first = true;
        for (Object element : array) {
            if (!first) {
                System.out.print(", ");
            }
            first = false;
            System.out.print(element);
        }

        System.out.println();
    }

    @SuppressWarnings("unused")
    public void dummy(boolean a, double[] b, byte[] c) {

    }
}
