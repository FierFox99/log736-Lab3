package Classes;

import Interfaces.ITransaction;

public class Transaction implements ITransaction {
        private long txID, clientID;
        private boolean confirmationState;

        public Transaction(long clientID){
                txID = clientID;
        }

        public void setConfirmed(boolean isConfirmed){
                confirmationState = isConfirmed;
        }
        
        public boolean isConfirmed(){
                return confirmationState;
        }

}

