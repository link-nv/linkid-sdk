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
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebSession;
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

        /**
         * {@inheritDoc}
         */
        @Override
        public Class<? extends Page> getHomePage() {

            return MyPage.class;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Session newSession(Request request, Response response) {

            return new MySession(request);
        }
    }

    public static class MySession extends WebSession {

        public MySession(Request request) {

            super(request);
        }

        public static MySession get() {

            return (MySession) Session.get();
        }


        private static final long serialVersionUID = 1L;
        private String            name;


        public void setName(String name) {

            this.name = name;
        }

        public String getName() {

            return name;
        }
    }

    public static class MyPage extends WebPage {

        public MyPage() {

            if (MySession.get().getName() != null) {
                Session.get().invalidateNow();
                throw new RestartResponseException(getClass());
            }
        }
    }


    @Test
    public void wicketTest() {

        WicketTester wicket = new WicketTester(new MyApp());
        wicket.processRequestCycle();

        MySession.get().setName("foo");
        wicket.processRequestCycle();
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
