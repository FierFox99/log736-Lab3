package Classes;

import java.util.List;
import Interfaces.*;

public class Block implements IBlock {
    // si previousHash = null, cela signifie que ce bloc est à la profondeur la plus haute possible (donc le bloc le moins profond de la blockchain)
    private String previousHash; // contient la valeur hashé représentant le bloc précédent (en terme de profondeur)
    private String merkleRoot; // hash du bloc actuel?
    private int nounce = 0; // (la valeur de départ n'est pas très importante tant qu'il s'agit d'un int) (puisque cette propriété est juste un compteur qui changera de valeur lorsqu'on essaiera de hasher le bloc)
    private long timestamp; // TODO trouver à quoi cette propriété sert
    private int depth; // la profondeur du bloc (ou son numéro dans le blockchain)
    private List<Integer> transactions;
    private String blockHash = null; // hash du bloc actuel?

    // Fonction appelée si on crée un nouveau bloc à pars entière (un bloc Genèse) (donc crée une nouvelle blockchain)
    public Block(List<Integer> transactions, long timestamp) {
        previousHash = null;
        depth = 0;
        this.transactions = transactions;
        this.timestamp = timestamp;
    }

    // Fonction appelée si on fait juste rajouté une profondeur de bloc à partir d'un bloc déjà existant
    public Block(String previousHash, List<Integer> transactions, int depth, long timestamp) {
        this.previousHash = previousHash;
        this.depth = depth; // le bloc précédent devrait avoir incrémenté de 1 la valeur dans l'appel de ce constructeur
        this.transactions = transactions;
        this.timestamp = timestamp;
    }

    public void setNounce(int nounce) {
        this.nounce = nounce;
    }

    // hash les transactions incluses dans ce bloc
    public void calculateRoot() {
        merkleRoot = HashfromString.sha256Hash(Integer.toString(depth) + transactions.toString() + Integer.toString(nounce)); // (il faut utiliser cette fonction pour hasher les transactions dans cette méthode)
    }

    // Génère le hash représentant ce block pour pouvoir le passer à la prochaine profondeur de ce bloc
    public String calculateBlockHash() {
        while (true) {
            calculateRoot(); // calcule un hashage pour ce bloc selon les 3 critères/variables utilisées dans la fonction

            if (merkleRoot.startsWith("0000")) {
                // si le hashage commence par 4 zéros, cela signifie qu'on a trouvé un bon hashage, donc on arrête

                blockHash = merkleRoot;
                return blockHash;
            }

            // on incrémente le nounce pour essayer d'obtenir un hash commençant par 4 zéros pour ce bloc
            setNounce(nounce + 1);
        }
    }





}
