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
package test.integ.net.link.safeonline.performance;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import net.link.safeonline.performance.entity.ExecutionEntity;


/**
 * <h2>{@link QuickTest}<br>
 * <sub>[in short] (TODO).</sub></h2>
 *
 * <p>
 * [description / usage].
 * </p>
 *
 * <p>
 * <i>Mar 12, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
public class QuickTest extends AbstractDataTest {

    @Override
    protected void configure() {

        this.DB_HOST = "sebeco-dev-12";
    }

    @SuppressWarnings("unchecked")
    public QuickTest() {

        ExecutionEntity execution = this.executionService.getExecution(new Date(1204796106 * 1000l));

        Query query = this.em.createQuery("SELECT d.profile.driverName" + "    FROM ProfileDataEntity d"
                + "        JOIN d.scenarioTiming t" + "    WHERE t.execution = :execution"
                + "        AND t.startTime = :start");

        query.setParameter("execution", execution);
        query.setParameter("start", 1204796453397l);

        List<Object> ttt = query.getResultList();
        printResults("Results", ttt);
    }

    public static void printResults(String title, List<Object> ttt) {

        int total = 0;
        List<Integer> maxlens = new ArrayList<Integer>();
        for (Object _tt : ttt) {
            Object[] tt;
            try {
                tt = (Object[]) _tt;
            } catch (ClassCastException e) {
                tt = new Object[] { _tt };
            }

            int rowTotal = 0;
            for (int i = 0; i < tt.length; ++i) {
                int length = tt[i].toString().length();
                rowTotal += length + 4;

                if (maxlens.size() < i + 1) {
                    maxlens.add(length);
                } else {
                    maxlens.set(i, Math.max(maxlens.get(i), length));
                }
            }

            total = Math.max(total, rowTotal);
        }

        System.out.println();
        String descr = String.format("%s (%d):", title, ttt.size());
        for (int i = 0; i < (total - descr.length()) / 2; ++i) {
            System.out.print(" ");
        }
        System.out.println(descr);
        for (int i = 0; i < total; ++i) {
            System.out.print("_");
        }
        System.out.println();

        for (Object _tt : ttt) {
            Object[] tt;
            try {
                tt = (Object[]) _tt;
            } catch (ClassCastException e) {
                tt = new Object[] { _tt };
            }

            for (int i = 0; i < tt.length; ++i) {
                System.out.format("|%" + (maxlens.get(i) + 1) + "s |", tt[i]);
            }
            System.out.println();
        }

        System.out.println();
    }

    public static void main(String[] args) {

        new QuickTest();
    }
}
