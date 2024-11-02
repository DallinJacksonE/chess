package dataaccess;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.Service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class DataAccessTests {
    private DataInterface db;
    private Service service;

    @BeforeEach
    public void setUp() {
        db = new SimpleLocalDataBase();
        service = new Service(db);

    }

    @Test
    void addUserTest() {
        
    }

}