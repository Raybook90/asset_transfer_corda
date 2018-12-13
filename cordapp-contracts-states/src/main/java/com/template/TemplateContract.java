package com.template;

import net.corda.core.contracts.Command;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

// ************
// * Contract *
// ************
public class TemplateContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String ID = "com.template.TemplateContract";

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    @Override
    public void verify(LedgerTransaction tx) throws IllegalArgumentException {
        if(tx.getCommands().size()>1){
            throw new IllegalArgumentException("Commands size greater than 1");
        }
        if(!(tx.getCommand(0).getValue() instanceof TemplateContract.Commands.Create)){
            throw new IllegalArgumentException("Not of instance create");
        }
        //throw new IllegalArgumentException("Error");
    }

    // Used to indicate the transaction's intent.
    public interface Commands extends CommandData {
        class Create implements Commands {}
    }
}