package Classes;

import java.io.IOException;

public class Simulation{
    public static void main(String[] args) throws IOException{

        Miner miner1 = new Miner(1);
        Miner miner2 = new Miner(2);
        Miner miner3 = new Miner(3);

        Client client1 = new Client(1,1);
        client1.init();
        client1.sendTransaction();
        Client client2 = new Client(2,2);

    }
}