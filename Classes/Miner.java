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
    private ArrayList<IBlock> blockchain;
    private ArrayList<ArrayList<IBlock>> branches;
    private ArrayList<Integer> neighborNodes;
    private List<Transaction> mempool;

    public Miner (int id) {
        this.id = id;
        allMiners.add(this);
    }

    // returns the list of all existing miners (except the one we are currently in)
    // (so returns the list of miners that this miner should be connected to)
    public ArrayList<Miner> getAllOtherMiners() {
        ArrayList<Miner> otherMiners = new ArrayList<>();

        for (Miner miner : allMiners) {
            if (miner == this)
                continue;

            otherMiners.add(miner);
        }

        return otherMiners;
    }

    //used whenever a miner finds a branch longer than its blockchain
    private void setLongestChain(){

    }

    //generates a new block, finds the corresponding PoW and sets the block's hash and nounce
    private IBlock mineBlock(){

    }

    private void addBlock() {

    }

    private boolean validateBlock(IBlock previousBlock, IBlock currentBlock){

    }

    private void addToMemPool(Transaction tx){}

    //returns the genesis block
    public ArrayList<IBlock> init() throws IOException {
    }

    //returns a list of connected nodes
    public ArrayList<Integer> connect() throws IOException {

    }

    //calls mineBlock method whenever it collects transactions and validates received blocks and adds it to the current chain
    public void listenToNetwork()throws IOException{

    }

    public ArrayList<Block> synchronise() throws IOException{

    }


}
