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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public void testFormat() {

        String format = "%s %l %s %05d %s";

        System.out.println("'" + localize(format, "foo", "bar", "foobar", 21378, "end") + "'");
    }


    // %[argument_index$][flags][width][.precision][t]conversion
    private static final String formatSpecifier = "%(\\d+\\$)?([-#+ 0,(\\<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])";
    private static Pattern      fsPattern       = Pattern.compile(formatSpecifier);


    protected String localize(String format, Object... args) {

        List<Object> localizationData = new ArrayList<Object>(args.length);
        StringBuffer newFormat = new StringBuffer(format);
        Matcher specifiers = fsPattern.matcher(format);

        int pos = 0, num = 0;
        while (specifiers.find(pos)) {
            if ("l".equalsIgnoreCase(specifiers.group(6))) {
                if ("L".equals(specifiers.group(6))) {
                    newFormat.setCharAt(specifiers.end(6) - 1, 'S');
                } else {
                    newFormat.setCharAt(specifiers.end(6) - 1, 's');
                }

                localizationData.add("@" + String.valueOf(args[num]));
            } else {
                localizationData.add(args[num]);
            }

            ++num;
            pos = specifiers.end();
        }

        System.err.println("old format: " + format);
        System.err.println("new format: " + newFormat.toString());
        System.err.print("old data: ");
        print(args);
        System.err.print("new data: ");
        print(localizationData.toArray());

        return String.format(newFormat.toString(), localizationData.toArray());
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
