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

    public static class A extends B {

        static {
            System.out.println("static-a");
        }

        {
            System.out.println("pre-a");
        }


        public A() {

            System.out.println("a");
        }
    }

    public static class B {

        static {
            System.out.println("static-b");
        }

        {
            System.out.println("pre-b");
        }


        public B() {

            System.out.println("b");
        }
    }


    @Test
    public void testInheritingConstructors() {

        new A();
    }

    void print(Object[] array) {

        boolean first = true;
        for (Object element : array) {
            if (!first) {
                System.err.print(", ");
            }
            first = false;
            System.err.print(element);
        }

        System.err.println();
    }
}
