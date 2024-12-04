package AlgoBitcoin;

import AlgoBitcoin.Classes.Miner;
import AlgoBitcoin.Classes.Transaction;
import AlgoBitcoin.Classes.Block;
import AlgoBitcoin.Classes.Custom.ConnectionInterMiners;
import AlgoBitcoin.Interfaces.IBlock;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class AlgoBitcoinTest {

    // Miner tests
    @Test
    public void testMinerAtCreation() throws IOException {
        // Test to check that miner initializes with a valid port and all other miners are set up
        Miner miner = new Miner(1);
        Miner miner2 = new Miner(2);

        assertEquals("Le port obtenu par chaque mineur devrait suivre la logique d'assignation de ports des mineurs.", 0, miner.getPort() % 100);
        assertEquals("Le port obtenu par chaque mineur devrait suivre la logique d'assignation de ports des mineurs.", miner.getPort() + 100, miner2.getPort());
        assertEquals("La collection retournée par cette fonction doit contenir l'autre mineur.", 1, miner.getAllOtherMiners().size());
        assertTrue("La collection retournée par cette fonction doit contenir l'autre mineur.", miner.getAllOtherMiners().contains(miner2));
        assertEquals("La collection retournée par cette fonction doit contenir l'autre mineur.", 1, miner2.getAllOtherMiners().size());
        assertTrue("La collection retournée par cette fonction doit contenir l'autre mineur.", miner2.getAllOtherMiners().contains(miner));
    }

    // Transaction tests
    @Test
    public void testTransactionIdUniqueness() {
        // Test to ensure each transaction has a unique ID
        Transaction t1 = new Transaction(1);
        Transaction t2 = new Transaction(1);
        Transaction t3 = new Transaction(2);

        assertNotEquals("Les ids des transactions devraient être uniques", t2.transactionId, t3.transactionId);
        assertNotEquals("Les ids des transactions devraient être uniques", t1.transactionId, t3.transactionId);
        assertNotEquals("Les ids des transactions devraient être uniques même si elles proviennent du même client", t1.transactionId, t2.transactionId);
    }

    @Test
    public void testSetConfirmed() {
        // Test to check that a transaction is not confirmed by default and can be confirmed using setConfirmed()
        Transaction transaction = new Transaction(1);
        assertFalse("Transaction should not be confirmed by default.", transaction.isConfirmed());
        
        transaction.setConfirmed();
        assertTrue("Transaction should be confirmed after calling setConfirmed().", transaction.isConfirmed());
    }

    @Test
    public void testTransactionSerialization() throws IOException, ClassNotFoundException {
        // Test to ensure that a serialized transaction can be deserialized and match the original transaction
        Transaction originalTransaction = new Transaction(1);
        String serialized = originalTransaction.serializeThisTransaction();

        assertNotNull("Serialized transaction should not be null.", serialized);
        assertFalse("Serialized transaction should not be empty.", serialized.isEmpty());

        Transaction deserializedTransaction = Transaction.deserializeTransaction(serialized);

        assertNotNull("Deserialized transaction should not be null.", deserializedTransaction);
        assertEquals("Deserialized transaction should match the original.", originalTransaction.transactionId, deserializedTransaction.transactionId);
    }

    // Block tests
    @Test
    public void testBlockChainInitialization() throws IOException {
        // Test to ensure that a block is initialized properly with an empty list of transactions
        Miner miner = new Miner(1);
        miner.init();

        ArrayList<IBlock> blockChain = Miner.getBlockchain();

        assertFalse("Le blockchain ne devrait pas être vide après qu'on l'ait initialisé", blockChain.isEmpty());
        assertEquals("Le blockchain devrait seulement contenir le bloc de genèse", 1, blockChain.size());

        Block blockGenese = (Block) blockChain.get(0);

        assertNotNull("Le bloc genèse ne peut pas être null.", blockGenese);
        assertNull("Le bloc genèse devrait ne pas avoir de blocs qui le précède.", blockGenese.getPreviousHash());
        assertEquals("Le bloc genèse devrait être haut niveau le plus haut possible.", 1, blockGenese.depth);
    }

    @Test
    public void testProofOfWork() {
        // Test to verify that the block hash is calculated correctly and is not null or empty
        Block block = new Block(new ArrayList<>());

        String hash = block.calculateBlockHash();

        assertNotNull("Block hash should not be null.", hash);
        assertEquals("Le hashage devrait être cohérent entre le block et la valeur retournée par la fonction", block.merkleRoot, hash);
        assertTrue("Le hashage doit commencer par un certain nombre de 0 dépendamment du niveau de difficulté des calculs.", hash.startsWith("0".repeat(Miner.difficulty)));
    }

    @Test
    public void testBlockSerialization() throws IOException, ClassNotFoundException {
        // Test to verify that a block can be serialized and deserialized correctly while preserving its hash
        Block block = new Block(new ArrayList<>());
        String serialized = block.serializeThisBlock();

        assertNotNull("Serialized block should not be null.", serialized);
        assertFalse("Serialized block should not be empty.", serialized.isEmpty());
        
        Block deserializedBlock = Block.deserializeBlock(serialized);
        assertNotEquals("Deserialized block should not be null.", null, deserializedBlock);
        assertEquals("Deserialized block should match the original.", block.getCurrentHash(), deserializedBlock.getCurrentHash());
    }
}
