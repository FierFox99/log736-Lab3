package AlgoBitcoin.Interfaces;

import AlgoBitcoin.Classes.Block;

import java.io.IOException;
import java.util.ArrayList;

public interface IMiner {
    //set target's first zeros number
    int difficulty = 5;

    //returns the genesis block
    ArrayList<IBlock> init() throws IOException;

    //returns a list of connected nodes
    ArrayList<Integer> connect() throws IOException;

    //calls mineBlock method whenever it collects transactions and validates received blocks and adds it to the current chain
    void listenToNetwork()throws IOException;

    ArrayList<Block> synchronise () throws IOException;
}
