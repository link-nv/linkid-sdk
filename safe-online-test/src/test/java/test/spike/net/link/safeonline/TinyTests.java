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

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
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

    public static class MyApp extends WebApplication {

        @Override
        public Class<? extends Page> getHomePage() {

            return MyPage.class;
        }
    }

    public static class MyPage extends WebPage {

        public MyPage() {

            add(new MyForm("form"));
        }


        class MyForm extends Form<String> {

            private static final long serialVersionUID = 1L;


            public MyForm(String id) {

                super(id);

                TextField<String> f = new TextField<String>("field");
                f.setRequired(true);
                add(f);
            }
        }
    }


    @Test
    public void wicketTest() {

        WicketTester wicket = new WicketTester(new MyApp());
        wicket.processRequestCycle();

        FormTester form = wicket.newFormTester("form");
        form.submit();

        wicket.assertErrorMessages(new String[] { "Field 'field' is required." });

        form = wicket.newFormTester("form");
        form.setValue("field", "foo");
        form.submit();

        wicket.assertNoErrorMessage();
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
