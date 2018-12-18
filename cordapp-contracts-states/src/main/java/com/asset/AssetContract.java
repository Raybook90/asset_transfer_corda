package com.asset;

import net.corda.core.contracts.Command;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

// ************
// * Contract *
// ************
public class AssetContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String ID = "com.asset.AssetContract";

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    @Override
    public void verify(LedgerTransaction tx) throws IllegalArgumentException {
        if(tx.getCommands().size()>1){
            throw new IllegalArgumentException("Commands size greater than 1");
        }
        Command<CommandData> command = tx.getCommand(0);
        if((command.getValue() instanceof AssetContract.Commands.Create)){
            if(tx.getInputs().size()>0) throw new IllegalArgumentException("Input not empty");
            if(tx.getOutputs().size()>1) throw new IllegalArgumentException("Invalid output");
            AssetState state = (AssetState) tx.getOutput(0);
            if(!command.getSigners().contains(state.getOwner().getOwningKey())) throw new IllegalArgumentException("Owner is not a signer");
            if(!command.getSigners().contains(state.getValidator().getOwningKey())) throw new IllegalArgumentException("Validator is not a signer");
        }
        else if((command.getValue() instanceof AssetContract.Commands.Transfer)){
            if(tx.getInputs().size()>1) throw new IllegalArgumentException("Input not empty");
            if(tx.getOutputs().size()>1) throw new IllegalArgumentException("Invalid output");

            final AssetState assetInput = tx.inputsOfType(AssetState.class).get(0);
            final AssetState assetOuput = tx.outputsOfType(AssetState.class).get(0);
            if(!command.getSigners().contains(assetOuput.getOwner().getOwningKey())) throw new IllegalArgumentException("Owner is not a signer");
            if(!command.getSigners().contains(assetInput.getOwner().getOwningKey())) throw new IllegalArgumentException("New owner is not a signer");
            if(!command.getSigners().contains(assetInput.getValidator().getOwningKey())) throw new IllegalArgumentException("Validator is not a signer");
            if(!assetInput.getValidator().equals(assetOuput.getValidator())) throw new IllegalArgumentException("Validator changed");
            if(!assetInput.getName().equals(assetOuput.getName())) throw new IllegalArgumentException("Name changed");

        }
        else {
            throw new IllegalArgumentException("Error, Command not found");
        }
    }

    // Used to indicate the transaction's intent.
    public interface Commands extends CommandData {
        class Create implements Commands {}
        class Transfer implements Commands {}

    }
}