package org.boon.datarepo;

import org.testng.annotations.Test;

import java.util.*;

import static org.boon.Boon.puts;
import static org.boon.criteria.ObjectFilter.and;
import static org.boon.criteria.ObjectFilter.eq;
import static org.boon.criteria.ObjectFilter.gt;
import static org.testng.Assert.assertTrue;

public class DataRepoPerfTest
{
    static final int MAX_N = 100000;
    static final int ITER = 100;
    static String[] jobs = new String[] {"manager", "clerk", "footballer", "artist", "teacher"};
    static String[] colours = new String[] {"red", "blue", "green", "yellow", "orange"};
    static String[] sports = new String[] {"athletics", "soccer", "swim", "cycle", "couch potato"};

    @Test
    public void init()
    {
        final Repo<String, Map<String, Object>> dbRepo =  (Repo<String, Map<String, Object>>)(Object) Repos.builder()
                .primaryKey("name")
                .lookupIndex("sport")
                .lookupIndex("job")
                .lookupIndex("experience")
                .lookupIndex("colour")
                .build(String.class, Map.class);

        final List<Map<String, Object>> database = new ArrayList<Map<String, Object>>();

        for (int i=0; i<MAX_N; i++){
            Map<String, Object> entry = new HashMap<String, Object>();
            entry.put("name","Mr "+i);
            entry.put("colour",colours[((int) (Math.random() * colours.length))]);
            entry.put("job",jobs[((int) (Math.random() * jobs.length))]);
            entry.put("sport",sports[((int) (Math.random() * sports.length))]);
            entry.put("experience", i%100);
            database.add(entry);
        }

        dbRepo.addAll(database);

        timeTask(dbRepo, "Brute force lookup", 1200, 10000, new RunThing() {
            public Object go(Object param) {
                List results;
                results = new ArrayList();
                for (Map<String, Object> entry : database) {
                    if (entry.get("colour").equals("red")) {
                        results.add(entry);
                    }
                }
                return results;
            }
        });

        timeTask(dbRepo, "Brute force number lookup", 900, 800, new RunThing() {
            public Object go(Object param) {
                List results = new ArrayList();
                for (Map<String, Object> entry : database) {
                    if ( (Integer)entry.get("experience") > 98) {
                        results.add(entry);
                    }
                }
                return results;
            }
        });

        timeTask(dbRepo, "Brute force number/compareTo lookup", 700, 10000, new RunThing() {
            Integer fifty = 50;
            public Object go(Object param) {
                List results = new ArrayList();
                for (Map<String, Object> entry : database) {
                    if ( fifty.compareTo ((Integer)entry.get("experience")) > 0 ) {
                        results.add(entry);
                    }
                }
                return results;
            }
        });

        timeTask(dbRepo, "DataRepo colour query", 60, 10000, new RunThing() {
            public Object go(Object param) {
                return dbRepo.query(eq("colour", "red"));
            }
        });

        timeTask(dbRepo, "DataRepo job query", 60, 10000, new RunThing() {
            public Object go(Object param) {
                return dbRepo.query(eq("job", "artist"));
            }
        });

        timeTask(dbRepo, "DataRepo AND lookup", 1450, 1000, new RunThing() {
            public Object go(Object param) {
                return dbRepo.query(and(eq("job", "artist"), eq("colour", "red"))); // TODO SLOW -dont use
            }
        });

        timeTask(dbRepo, "DataRepo filtered lookup", 650, 1000, new RunThing() {
            public Object go(Object param) {
                return dbRepo.results(eq("sport", "swim")).filter(eq("colour", "red")); // TODO SLOW - dont use
            }
        });

        timeTask(dbRepo, "DataRepo lookup and manual", 500, 1000, new RunThing() {
            public Object go(Object param) {
                Collection<Map<String, Object>> results2 = new ArrayList<Map<String, Object>>(); // TODO SLOW - dont use
                ResultSet<Map<String, Object>> results = dbRepo.results(eq("sport", "swim")); // TODO SLOW - dont use
                for (Map<String, Object> result : results) {
                    if (result.get("colour").equals("red")) {
                        results2.add(result);
                    }
                }
                return results2;
            }
        });

        timeTask(dbRepo, "DataRepo number range", 5000, 100, new RunThing() {
            public Object go(Object param) {
                return dbRepo.query(gt("experience", 98)); // TODO VERY SLOW
            }
        });

        puts("done");
    }

    private void timeTask(Repo<String, Map<String, Object>> dbRepo, String task, int expectedMaxTime, int expectedMinSize, RunThing job) {
        long start;
        Collection<Map<String, Object>> results2;
        long elapsed;
        start = System.currentTimeMillis();
        results2 = null;
        // DataRepo way
        for (int i=0; i<ITER; i++)
        {
            results2 = (Collection<Map<String, Object>>) job.go(null);
        }
        elapsed = System.currentTimeMillis() - start;
//        assertTrue(elapsed < expectedMaxTime, elapsed+" vs "+expectedMaxTime);
        assertTrue(results2.size() > expectedMinSize, results2.size()+" vs "+expectedMinSize);
        puts(task, elapsed, "ms, found:", results2.size());
    }
}