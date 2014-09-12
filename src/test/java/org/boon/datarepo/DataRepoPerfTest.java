package org.boon.datarepo;

import org.testng.annotations.Test;

import java.util.*;

import static org.boon.Boon.puts;
import static org.boon.criteria.ObjectFilter.and;
import static org.boon.criteria.ObjectFilter.eq;
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
        Repo<String, Map<String, String>> dbRepo =  (Repo<String, Map<String, String>>)(Object) Repos.builder()
                .primaryKey("name")
                .lookupIndex("sport")
                .lookupIndex("job")
                .lookupIndex("colour")
                .build(String.class, Map.class);

        List<Map<String, String>> database = new ArrayList<Map<String, String>>();

        for (int i=0; i<MAX_N; i++){
            Map<String, String> entry = new HashMap<String, String>();
            entry.put("name","Mr "+i);
            entry.put("colour",colours[((int) (Math.random() * colours.length))]);
            entry.put("job",jobs[((int) (Math.random() * jobs.length))]);
            entry.put("sport",sports[((int) (Math.random() * sports.length))]);
            database.add(entry);
        }

        dbRepo.addAll(database);

        long start = System.currentTimeMillis();
        List results = null;
        // time non-DataRepo way
        for (int i=0; i<ITER; i++)
        {
            results = new ArrayList();
            for (Map<String, String> entry : database) {
                if (entry.get("colour").equals("red")) {
                    results.add(entry);
                }
            }
        }
        long elapsed = System.currentTimeMillis() - start;
        assertTrue(elapsed > 300);
        assertTrue(results.size() > 10000,"Checking "+results.size()+" vs 10,000");
        puts("Brute force lookup", elapsed, "ms, found:", results.size());

        start = System.currentTimeMillis();
        results = null;
        // DataRepo way
        for (int i=0; i<ITER; i++)
        {
            results = dbRepo.query(eq("colour","red"));
        }
        elapsed = System.currentTimeMillis() - start;
        assertTrue(elapsed < 110, elapsed+" vs 110");
        assertTrue(results.size() > 10000);
        puts("DataRepo lookup", elapsed, "ms, found:", results.size());

        start = System.currentTimeMillis();
        results = null;
        // DataRepo way
        for (int i=0; i<ITER; i++)
        {
            results = dbRepo.query(eq("job","artist"));
        }
        elapsed = System.currentTimeMillis() - start;
        assertTrue(elapsed < 30);
        assertTrue(results.size() > 10000);
        puts("DataRepo lookup", elapsed, "ms, found:", results.size());

        start = System.currentTimeMillis();
        Collection<Map<String, String>> results2 = null;
        // DataRepo way
        for (int i=0; i<ITER; i++)
        {
            results2 = dbRepo.query(and(eq("job", "artist"), eq("colour", "red"))); // TODO SLOW -dont use
        }
        elapsed = System.currentTimeMillis() - start;
        assertTrue(elapsed < 1150, elapsed+" vs 1150");
        assertTrue(results2.size() > 1000, results2.size()+" vs 1000");
        puts("DataRepo AND lookup", elapsed, "ms, found:", results2.size());

        start = System.currentTimeMillis();
        results2 = null;
        // DataRepo way
        for (int i=0; i<ITER; i++)
        {
            results2 = dbRepo.results(eq("sport", "swim")).filter(eq("colour", "red")); // TODO SLOW - dont use
        }
        elapsed = System.currentTimeMillis() - start;
        assertTrue(elapsed < 650, elapsed+" vs 150");
        assertTrue(results2.size() > 1000, results2.size()+" vs 1000");
        puts("DataRepo filtered lookup", elapsed, "ms, found:", results2.size());
    }
}