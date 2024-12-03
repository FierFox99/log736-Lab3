package AlgoBitcoin;

import AlgoBitcoin.Classes.Miner;
import AlgoBitcoin.Classes.Transaction;
import AlgoBitcoin.Classes.Block;
import AlgoBitcoin.Classes.Custom.ConnectionInterMiners;
import AlgoBitcoin.Interfaces.IBlock;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class AlgoBitcoinTest {

    // Miner tests
    @Test
    public void testMinerInitialization() throws IOException {
        // Test to check that miner initializes with a valid port and all other miners are set up
        Miner miner = new Miner(1);
        assertEquals(0, miner.getPort() % 100, "Port initialization should start with the correct offset.");
        assertNotNull(miner.getAllOtherMiners(), "Neighbor miners should be initialized.");
    }

    @Test
    public void testMinerBlockchainInitialization() throws IOException {
        // Test to verify that the miner's blockchain contains the genesis block upon initialization
        Miner miner = new Miner(1);
        ArrayList<IBlock> blockchain = miner.init();
        assertEquals(1, blockchain.size(), "Blockchain should contain the genesis block after initialization.");
    }

    // Transaction tests
    @Test
    public void testTransactionIdUniqueness() {
        // Test to ensure each transaction has a unique ID
        Transaction t1 = new Transaction(1);
        Transaction t2 = new Transaction(2);

        assertNotEquals(t1.transactionId, t2.transactionId, "Transaction IDs should be unique.");
    }

    @Test
    public void testSetConfirmed() {
        // Test to check that a transaction is not confirmed by default and can be confirmed using setConfirmed()
        Transaction transaction = new Transaction(1);
        assertFalse(transaction.isConfirmed(), "Transaction should not be confirmed by default.");
        
        transaction.setConfirmed();
        assertTrue(transaction.isConfirmed(), "Transaction should be confirmed after calling setConfirmed().");
    }

    @Test
    public void testSerializeTransaction() throws IOException {
        // Test to verify that the transaction can be serialized into a non-null, non-empty string
        Transaction transaction = new Transaction(1);
        String serialized = transaction.serializeThisTransaction();
        
        assertNotNull(serialized, "Serialized transaction should not be null.");
        assertFalse(serialized.isEmpty(), "Serialized transaction should not be empty.");
    }

    @Test
    public void testDeserializeTransaction() throws IOException, ClassNotFoundException {
        // Test to ensure that a serialized transaction can be deserialized and match the original transaction
        Transaction originalTransaction = new Transaction(1);
        String serialized = originalTransaction.serializeThisTransaction();

        Transaction deserializedTransaction = Transaction.deserializeTransaction(serialized);

        assertNotNull(deserializedTransaction, "Deserialized transaction should not be null.");
        assertEquals(originalTransaction.transactionId, deserializedTransaction.transactionId, "Deserialized transaction should match the original.");
    }

    // Block tests
    @Test
    public void testBlockInitialization() {
        // Test to ensure that a block is initialized properly with an empty list of transactions
        Block block = new Block(new ArrayList<>());
        assertNotNull(block, "Block should initialize correctly.");
        assertTrue(block.transactions.isEmpty(), "Block should have no transactions initially.");
    }

    @Test
    public void testBlockHashCalculation() {
        // Test to verify that the block hash is calculated correctly and is not null or empty
        Block block = new Block(new ArrayList<>());
        String hash = block.calculateBlockHash();
        assertNotNull(hash, "Block hash should not be null.");
        assertFalse(hash.isEmpty(), "Block hash should not be empty.");
    }

    @Test
    public void testBlockSerialization() throws IOException, ClassNotFoundException {
        // Test to verify that a block can be serialized and deserialized correctly while preserving its hash
        Block block = new Block(new ArrayList<>());
        String serialized = block.serializeThisBlock();
        
        assertNotNull(serialized, "Serialized block should not be null.");
        
        Block deserializedBlock = Block.deserializeBlock(serialized);
        assertNotNull(deserializedBlock, "Deserialized block should not be null.");
        assertEquals(block.getCurrentHash(), deserializedBlock.getCurrentHash(), "Deserialized block should match the original.");
    }

    // ConnectionInterMiners tests
    @Test
    public void testConnectionInterMinersInitialization() throws IOException {
        // Test to ensure that the ConnectionInterMiners initializes correctly
        ConnectionInterMiners connection = new ConnectionInterMiners(null, null, 0);
        assertNotNull(connection, "ConnectionInterMiners should initialize properly.");
    }
}
