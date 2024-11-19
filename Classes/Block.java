package Classes;

import java.util.List;
import Interfaces.*;

public class Block implements IBlock {
    private String previousHash;
    private String root;
    private int nounce;
    private long timestamp;
    private int depth;
    private List<Integer> transactions;


    public Block(String previousHash, List<Integer> transactions, int depth, long timestamp){}

    public void setNounce(int nounce){}

    public void calculateRoot(){}
    
    public String calculateBlockHash(){}





}
