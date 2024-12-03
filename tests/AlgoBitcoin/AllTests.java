package AlgoBitcoin;

import AlgoBitcoin.Classes.Client;
import AlgoBitcoin.Classes.HashfromString;
import AlgoBitcoin.Classes.Miner;
import AlgoBitcoin.Classes.Transaction;
import org.junit.*;

import static org.junit.Assert.*;

public class AllTests {

    // Client Class Tests
    @Test
    public void testClientInitialization() {
        Client client = new Client(1, 3000);
        assertNotNull(client);
    }

    @Test
    public void testClientConnectionToMiner() {
        Client client = new Client(1, 3000);
        try {
            int result = client.init();
            assertEquals(1, result);
        } catch (Exception e) {
            fail("Exception occurred during client initialization: " + e.getMessage());
        }
    }

    // HashfromString Class Tests
    @Test
    public void testSha256Hashing() {
        String input = "TestString";
        String hash = HashfromString.sha256Hash(input);
        assertNotNull(hash);
        assertEquals(64, hash.length());
    }

    // Miner Class Tests
    @Test
    public void testMinerInitialization() {
        try {
            Miner miner = new Miner(1);
            assertNotNull(miner);
        } catch (Exception e) {
            fail("Exception occurred during miner initialization: " + e.getMessage());
        }
    }

    // Transaction Class Tests
    @Test
    public void testTransactionCreation() {
        Transaction transaction = new Transaction(1001);
        assertEquals(1001, transaction.clientId);
        assertFalse(transaction.isConfirmed());
    }

    @Test
    public void testTransactionConfirmation() {
        Transaction transaction = new Transaction(1001);
        transaction.setConfirmed();
        assertTrue(transaction.isConfirmed());
    }
}
