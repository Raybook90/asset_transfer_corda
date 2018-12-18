package com.asset;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.utilities.ProgressTracker;

import java.util.DuplicateFormatFlagsException;
import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireThat;

// ******************
// * TransferResponder flow *
// ******************
@InitiatedBy(Transfer.class)
public class TransferResponder extends FlowLogic<Void> {
    private FlowSession counterpartySession;

    public TransferResponder(FlowSession counterpartySession) {
        this.counterpartySession = counterpartySession;
    }

    @Suspendable
    @Override
    public Void call() throws FlowException {
        // TransferResponder flow logic goes here.
        // getServiceHub().loadState(stx.tx.inputs.toSet());
        class SignTxFlow extends SignTransactionFlow {
            private SignTxFlow(FlowSession otherSession, ProgressTracker progressTracker) {
                super(otherSession, progressTracker);
            }

            @Override
            protected void checkTransaction(SignedTransaction stx) {
                requireThat(require -> {
                    // Any additional checking we see fit...
                    ContractState outputState = stx.getTx().getOutputs().get(0).getData();
                    assert (outputState instanceof AssetState);
                    AssetState state = (AssetState)outputState;
                    List<StateAndRef<AssetState>> states = getServiceHub().getVaultService().queryBy(AssetState.class).getStates();
                    try {
                        states.stream().filter(artStateAndRef -> {
                            AssetState assetState = artStateAndRef.getState().getData();
                            return assetState.getName().equals(state) && assetState.getOwner().equals(assetState.getOwner());
                        }).findAny().orElseThrow(() -> new IllegalArgumentException("The piece of asset was not found."));
                        throw new DuplicateFormatFlagsException("Similar Record already exists");
                    } catch (IllegalArgumentException err){

                    }
                    return null;
                });
            }
        }

        subFlow(new SignTxFlow(counterpartySession, SignTransactionFlow.tracker()));
        return null;
    }
}
