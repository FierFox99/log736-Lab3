package Classes;

import Interfaces.IBlock;
import Interfaces.IMiner;
import java.io.IOException;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

public class Miner implements IMiner {

    public final static int BLOCK_SIZE = 10;
    private long id;
    private DatagramSocket socket;
    private ArrayList<IBlock> blockchain;
    private ArrayList<ArrayList<IBlock>> branches;
    private ArrayList<Integer> neighborNodes;
    private List<Transaction> mempool;

    public Miner (long id){}

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
