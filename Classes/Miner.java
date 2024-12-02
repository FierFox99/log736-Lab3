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
    private List<Transaction> mempool = new ArrayList<>();

    public Miner(int id) {
        this.id = id;
        this.blockchain = new ArrayList<>();
        this.branches = new ArrayList<>();
        this.mempool = new ArrayList<>();
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
    }

    //generates a new block, finds the corresponding PoW and sets the block's hash and nounce
    private IBlock mineBlock(){
        if (mempool.isEmpty()) {
            System.out.println("Aucune transaction à miner pour le mineur " + id);
            return null;
        }

        IBlock previousBlock = blockchain.get(blockchain.size() - 1);
        Block newBlock = new Block(previousBlock.getHash(), mempool.subList(0, Math.min(BLOCK_SIZE, mempool.size())));

        // Preuve de travail simplifiée
        while (!newBlock.calculateHash().startsWith("0000")) {
            newBlock.incrementNonce();
        }

        mempool.clear(); // Vide la mempool après minage
        return newBlock;
    }

    private void addBlock() {
        blockchain.add(block);
        System.out.println("Bloc ajouté par le mineur " + id);
    }

    private boolean validateBlock(IBlock previousBlock, IBlock currentBlock){
        return currentBlock.getPreviousHash().equals(previousBlock.getHash()) && currentBlock.isValid();

    }

    private void addToMemPool(Transaction tx){
        mempool.add(tx);
        System.out.println("Transaction ajoutée à la mempool par le mineur " + id);
    }

    public ArrayList<IBlock> init() throws IOException {
        Block genesisBlock = new Block(null, new ArrayList<>());
        genesisBlock.setHash(genesisBlock.calculateHash());
        blockchain.add(genesisBlock);
        System.out.println("Bloc de genèse créé par le mineur " + id);
        return blockchain;
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
        for (Miner miner : getAllOtherMiners()) {
            if (miner.blockchain.size() > this.blockchain.size()) {
                this.blockchain = new ArrayList<>(miner.blockchain);
                System.out.println("Blockchain synchronisée avec le mineur " + miner.id + " par " + id);
            }
        }
        return blockchain;
    }
}
