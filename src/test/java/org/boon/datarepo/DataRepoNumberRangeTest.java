package org.boon.datarepo;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.boon.criteria.ObjectFilter.eq;
import static org.boon.criteria.ObjectFilter.gt;
import static org.testng.Assert.assertEquals;

public class DataRepoNumberRangeTest
{
    static final int MAX_N = 100000;
    static String[] jobs = new String[] {"manager", "clerk", "footballer", "artist", "teacher"};
    static String[] colours = new String[] {"red", "blue", "green", "yellow", "orange"};
    static String[] sports = new String[] {"athletics", "soccer", "swim", "cycle", "couch potato"};

    @Test
    public void init()
    {
        Repo<String, Map<String, Object>> dbRepo =  (Repo<String, Map<String, Object>>)(Object) Repos.builder()
                .primaryKey("name")
                .lookupIndex("sport")
                .lookupIndex("job")
                .lookupIndex("colour")
                .build(String.class, Map.class);

        List<Map<String, Object>> database = new ArrayList<Map<String, Object>>();

        for (int i=0; i<MAX_N; i++){
            Map<String, Object> entry = new HashMap<String, Object>();
            entry.put("name","Mr "+i);
            entry.put("colour",colours[(i % colours.length)]);
            entry.put("job",jobs[(i % jobs.length)]);
            entry.put("sport",sports[(i % sports.length)]);
            entry.put("experience", i%100);
            database.add(entry);
        }

        dbRepo.addAll(database);

        assertEquals(dbRepo.size(), MAX_N);

        List<Map<String, Object>> results = dbRepo.query(eq("job", "artist"));
        assertEquals(results.size(), 20000);

        results = dbRepo.query(eq("colour", "red"));
        assertEquals(results.size(), 20000);

        results = dbRepo.query(gt("experience", 50));
        assertEquals(results.size(), 49000);

    }
}