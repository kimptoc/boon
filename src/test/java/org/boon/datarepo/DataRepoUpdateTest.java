package org.boon.datarepo;

import org.boon.Maps;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.boon.criteria.ObjectFilter.eq;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class DataRepoUpdateTest
{
    static final int MAX_N = 10;
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
            entry.put("colour",colours[(i % colours.length)]);
            entry.put("job",jobs[(i % jobs.length)]);
            entry.put("sport",sports[(i % sports.length)]);
            database.add(entry);
        }

        dbRepo.addAll(database);

        assertEquals(dbRepo.size(), MAX_N);

        List<Map<String, String>> results = dbRepo.query(eq("job", "artist"));
        assertEquals(results.size(), 2);

        results = dbRepo.query(eq("colour", "red"));
        assertEquals(results.size(), 2);

        results = dbRepo.query(eq("colour", "reddo"));
        assertEquals(results.size(), 0);

        int orange = 0;
        results = dbRepo.query(eq("colour", "red"));
        assertEquals(results.size(),2);
        Map<String, String> result = results.get(0);
                Map updated = Maps.copy(result);
//                dbRepo.validateIndexes(result);
                dbRepo.update(updated);
//                dbRepo.update(result.get("name"),"colour","orange");
//                orange++;

//        dbRepo.modify(updated);

        results = dbRepo.query(eq("colour", "red"));
//        assertEquals(results.size(),1);
        results = dbRepo.query(eq("colour", "orange"));
//        assertEquals(results.size(), 1);
    }
}