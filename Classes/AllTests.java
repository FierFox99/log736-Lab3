package Classes;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class AllTests {

    // Block Class Tests
    @Test
    public void testGenesisBlockCreation() {
        Block genesisBlock = new Block(Arrays.asList(1, 2, 3));
        assertNull(genesisBlock.getPreviousHash());
        assertEquals(1, genesisBlock.depth);
    }

    @Test
    public void testBlockHashCalculation() {
        Block block = new Block("previousHashValue", Arrays.asList(1, 2, 3), 2);
        String blockHash = block.calculateBlockHash();
        assertNotNull(blockHash);
        assertTrue(blockHash.startsWith("0000"));
    }

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
