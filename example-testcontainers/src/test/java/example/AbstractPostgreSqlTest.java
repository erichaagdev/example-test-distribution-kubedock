package example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.SQLException;

public abstract class AbstractPostgreSqlTest {

    @Test
    public void test() throws SQLException {
        final var tableName = getClass().getSimpleName().toLowerCase();
        try (final var postgres = new PostgreSQLContainer<>("postgres:16.3")) {
            postgres.start();
            try (final var connection = postgres.createConnection("")) {
                try (final var statement = connection.createStatement()) {
                    statement.executeUpdate("CREATE TABLE " + tableName + " (my_value TEXT)");
                }
                try (final var statement = connection.prepareStatement("INSERT INTO " + tableName + " (my_value) VALUES (?)".formatted(tableName))) {
                    statement.setString(1, "foo");
                    Assertions.assertEquals(1, statement.executeUpdate());
                }
                try (final var statement = connection.createStatement();
                     final var resultSet = statement.executeQuery("SELECT * FROM " + tableName + " WHERE my_value = 'foo'")) {
                    Assertions.assertTrue(resultSet.next());
                    Assertions.assertEquals("foo", resultSet.getString("my_value"));
                }
            }
        }
    }

}
