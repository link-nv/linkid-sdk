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

    @Test
    public void testPrimitiveClassNames() {

        for (java.lang.reflect.Method m : getClass().getMethods())
            if (m.getName().equals("dummy")) {

                System.out.println("Generic Paramter Types:");
                print(m.getGenericParameterTypes());

                System.out.println("Paramter Types:");
                print(m.getParameterTypes());

                System.out.println("Type Paramters:");
                print(m.getTypeParameters());
            }
    }

    private void print(Object[] array) {

        boolean first = true;
        for (Object element : array) {
            if (!first)
                System.out.print(", ");
            first = false;
            System.out.print(element);
        }

        System.out.println();
    }

    @SuppressWarnings("unused")
    public void dummy(boolean a, double[] b, byte[] c) {

    }
}
