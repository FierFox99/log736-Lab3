package AlgoBitcoin.Classes;

import java.io.IOException;

public class Simulation{
    public static void main(String[] args) throws IOException{

        Miner miner1 = new Miner(1);
        Miner miner2 = new Miner(2);

        Client client1 = new Client(1,miner1.getPort());
        client1.sendTransaction();
        Client client2 = new Client(2,miner2.getPort());

    }
}