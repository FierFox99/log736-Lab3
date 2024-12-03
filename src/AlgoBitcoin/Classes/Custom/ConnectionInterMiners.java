package AlgoBitcoin.Classes.Custom;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ConnectionInterMiners {
    DatagramSocket socketConnection;
    InetAddress inetAddressOfTheOtherMiner;
    int portOfTheOtherMiner;

    public ConnectionInterMiners(DatagramSocket socket, InetAddress address, int port) {
        socketConnection = socket;
        inetAddressOfTheOtherMiner = address;
        portOfTheOtherMiner = port;
    }
}
