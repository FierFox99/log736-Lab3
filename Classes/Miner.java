package Classes;

import Interfaces.IBlock;
import Interfaces.IMiner;
import java.io.IOException;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

public class Miner implements IMiner {

    public final static int BLOCK_SIZE = 10;
    private static ArrayList<Miner> allMiners = new ArrayList<>();
    private int id;
    private DatagramSocket socket;
    private ArrayList<IBlock> blockchain = new ArrayList<>();
    private ArrayList<ArrayList<IBlock>> branches = new ArrayList<>();
    private ArrayList<Integer> neighborNodes = new ArrayList<>();
    private List<Transaction> mempool = new ArrayList<>(); // les transactions que nous avons reçu en attente d'être confirmés et insérés dans un bloc

    public Miner(int id) {
        this.id = id;
        allMiners.add(this);
    }

    public ArrayList<Miner> getAllOtherMiners() {
        ArrayList<Miner> otherMiners = new ArrayList<>();
        for (Miner miner : allMiners) {
            if (miner != this) {
                otherMiners.add(miner);
            }
        }
        return otherMiners;
    }

    //used whenever a miner finds a branch longer than its blockchain
    private void setLongestChain(){
        for (ArrayList<IBlock> branch : branches) {
            if (branch.size() > blockchain.size()) {
                blockchain = new ArrayList<>(branch);
                System.out.println("Chaîne la plus longue définie par le mineur " + id);
            }
        }

        branches.clear(); // on abandonne toutes les autres branches après avoir défini celle officielle
    }

    private IBlock mineBlock() {
        // Mining logic goes here.
        ArrayList<Transaction> transactionsToConfirm = (ArrayList<Transaction>) mempool;
        mempool.removeAll(transactionsToConfirm); // on fait ça de cette manière pour que cette opération supporte le fait qu'on puisse recevoir de nouvelles transactions même durant celle-ci

        // on converti les transactions à juste leurs ids
        List<Integer> idsOfTransactionsToConfirm = new ArrayList<>();

        for (Transaction t: transactionsToConfirm) {
            idsOfTransactionsToConfirm.add(t.transactionId);
        }

        Block newBlockToMine = null;

        if (blockchain.size() == 0) {
            // il n'y a pas d'autres blocks, donc nous allons créé/miné un bloc genèse
            newBlockToMine = new Block(new ArrayList<>());
        } else {
            // un bloc normal (pas de genèse)
            Block lastBlock = (Block) blockchain.get(blockchain.size() - 1);

            // on peut prendre directement son hash (puisque celui-ci doit déjà avoir été hashé lors de son proof of work/lors qu'il a été miné)
            newBlockToMine = new Block(lastBlock.blockHash, idsOfTransactionsToConfirm, lastBlock.depth);
        }

        /**
         * On mine
         */

        // proof-of-worf/on mine
        newBlockToMine.blockHash = newBlockToMine.calculateBlockHash();

        return newBlockToMine; // Replace with an actual IBlock implementation.
    }

    private void addBlock(Block block) {
        // Logic to add a block to the end of the chain.
        blockchain.add(block);
        System.out.println("Bloc ajouté par le mineur " + id);
    }

    private void addToMemPool(Transaction tx){
        mempool.add(tx);
        System.out.println("Transaction ajoutée à la mempool par le mineur " + id);
    }

    public ArrayList<IBlock> init() throws IOException {
        // Initialize the genesis block.
        Block newGenesisBlock = (Block) mineBlock();

        addBlock(newGenesisBlock);
        System.out.println("Bloc de genèse créé par le mineur " + id);

        return blockchain; // Return initialized blockchain.
    }

    public ArrayList<Integer> connect() throws IOException {
        ArrayList<Integer> connectedNodes = new ArrayList<>();
        for (Miner miner : getAllOtherMiners()) {
            connectedNodes.add(miner.id);
        }
        this.neighborNodes = connectedNodes;
        System.out.println("Mineur " + id + " connecté aux nœuds : " + connectedNodes);
        return connectedNodes;
    }

    //calls mineBlock method whenever it collects transactions and validates received blocks and adds it to the current chain
    public void listenToNetwork()throws IOException{
        // Simulation d'écoute réseau
        Object message = receiveMessage();
        if (message instanceof Transaction) {
            addToMemPool((Transaction) message);
        } else if (message instanceof IBlock) {
            IBlock receivedBlock = (IBlock) message;
            if (validateBlock(blockchain.get(blockchain.size() - 1), receivedBlock)) {
                addBlock(receivedBlock);
            } else {
                System.out.println("Bloc invalide reçu par le mineur " + id);
            }
        }
    }

    public ArrayList<Block> synchronise() throws IOException{
        return null; // TODO
    }

    private boolean validateBlock(IBlock previousBlock, IBlock currentBlock){
        return currentBlock.getPreviousHash().equals(previousBlock.getHash()) && currentBlock.isValid();

    }
}
