package org.boon.datarepo;

import org.boon.Maps;
import org.testng.annotations.Test;

import java.util.*;

import static org.boon.Boon.puts;
import static org.boon.criteria.ObjectFilter.eq;
import static org.testng.Assert.assertEquals;

public class DataRepoAmendTest {

    static List<Map<String, String>> database = java.util.Collections.synchronizedList(new ArrayList<Map<String, String>>());

    static String[] jobs = new String[] {"manager", "clerk", "footballer", "artist", "teacher"};
    static String[] colours = new String[] {"red", "blue", "green", "yellow", "orange"};
    static String[] sports = new String[] {"athletics", "soccer", "swim", "cycle", "couch potato"};

    @Test
    public void amends() {

        Repo<String, Map<String, String>> dbRepo = setup();

        Map<String, String> item = doAmend(dbRepo);

        puts("just amend object");
        puts("Expect to be 1 manager:", findBoon(dbRepo, "manager", "job").size());
        puts("Expect to be 3 clerks:", findBoon(dbRepo, "clerk", "job").size());

        dbRepo = setup();
        item = doAmend(dbRepo);
        puts("try update call");
        dbRepo.update(item);
        assertEquals(findBoon(dbRepo, "manager", "job").size(), 1,"Expect to be 1 manager");
        assertEquals(findBoon(dbRepo, "clerk", "job").size(), 3,"Expect to be 3 clerks");

        dbRepo = setup();
        item = doAmend(dbRepo);
        puts("try modify call");
        dbRepo.modify(item);
        assertEquals(findBoon(dbRepo, "manager", "job").size(), 1,"Expect to be 1 manager");
        assertEquals(findBoon(dbRepo, "clerk", "job").size(), 3,"Expect to be 3 clerks");
        puts("db size:", dbRepo.size());

        dbRepo = setup();
        List<Map<String, String>> managers1 = findBoon(dbRepo, "manager", "job");
        puts("try delete/add call");
        item = managers1.get(0);
        dbRepo.delete(item);
        item.put("job", "clerk");
        dbRepo.add(item);
        assertEquals(findBoon(dbRepo, "manager", "job").size(), 1,"Expect to be 1 manager");
        assertEquals(findBoon(dbRepo, "clerk", "job").size(), 3,"Expect to be 3 clerks");
        puts("db size:", dbRepo.size());

        dbRepo = setup();
        doAmend(dbRepo);
        puts("recreate db");
        dbRepo = createDataRepo();
        puts("Expect to be 1 manager:", findBoon(dbRepo, "manager", "job").size());
        puts("Expect to be 3 clerks:", findBoon(dbRepo, "clerk", "job").size());
        puts("db size:", dbRepo.size());

    }

    private Map<String, String> doAmend(Repo<String, Map<String, String>> dbRepo) {
        List<Map<String, String>> managers = findBoon(dbRepo, "manager", "job");
//        puts("Expect to be 2 managers:",managers.size());
//        puts("Expect to be 2 clerk:", findBoon(dbRepo, "clerk", "job").size());
//        puts("Expect to be 2 red:", findBoon(dbRepo, "red", "colour").size());

        Map<String, String> newItem = Maps.copy(managers.get(0));
        newItem.put("job", "clerk");
        return newItem;
    }

    private Repo<String, Map<String, String>> setup() {
        database = new ArrayList<Map<String, String>>();
        createEntry(database, 1, 0, 0, 0);
        createEntry(database, 2, 1, 1, 1);
        createEntry(database, 3, 0, 1, 2);
        createEntry(database, 4, 2, 0, 3);

        return createDataRepo();
    }

    private Repo<String, Map<String, String>> createDataRepo() {
        Repo<String, Map<String, String>> dbRepo =  (Repo<String, Map<String, String>>)(Object) Repos.builder()
                .primaryKey("name")
                .lookupIndex("sport")
                .lookupIndex("job")
                .lookupIndex("colour")
                .build(String.class, Map.class);

        dbRepo.addAll(database);
//        puts("db size:",dbRepo.size());
        return dbRepo;
    }

    private void createEntry(List<Map<String, String>> database, int id, int colour, int job, int sport) {
        Map<String, String> entry = new HashMap<String, String>();
        entry.put("name","Mr "+id);
        entry.put("colour",colours[colour]);
        entry.put("job",jobs[job]);
        entry.put("sport",sports[sport]);
        database.add(entry);
    }



    private List<Map<String, String>> findBoon(Repo<String, Map<String, String>> dbRepo, String value, String property) {
        return dbRepo.query(eq(property, value));
    }
}
