package AlgoBitcoin.Classes;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Simulation{
    public static void main(String[] args) throws IOException, InterruptedException {

        Miner miner1 = new Miner(1);
        Miner miner2 = new Miner(2);

        // on donne le temps à nos miners de setup leurs ports avant de tenter de se connecter à eux
        TimeUnit.SECONDS.sleep(2);

        miner1.connect();
        miner2.connect();

        miner1.init(); // a seulement besoin d'être appeler une fois pour initialiser tout le blockchain

        Client client1 = new Client(1,miner1.getPort());
        client1.sendTransaction();
        Client client2 = new Client(2,miner2.getPort());

    }
}