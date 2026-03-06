package org.mydotey.artemis.test;

public class ArtemisTest {

    static {
        App.main(new String[0]);
        try {
            // Initialize database schema for tests
            TestDatabaseInitializer.initDatabase();
        } catch (Exception e) {
            System.err.println("[ArtemisTest] Failed to initialize database schema: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
