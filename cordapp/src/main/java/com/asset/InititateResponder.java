package com.asset;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.utilities.ProgressTracker;

import java.util.DuplicateFormatFlagsException;
import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireThat;

// ******************
// * TransferResponder flow *
// ******************
@InitiatedBy(Initiator.class)
public class InititateResponder extends FlowLogic<SignedTransaction > {
    private FlowSession counterpartySession;

    public InititateResponder(FlowSession counterpartySession) {
        this.counterpartySession = counterpartySession;
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
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
                    require.using("This must be an Asset transaction.", outputState instanceof AssetState);

                    AssetState state = (AssetState)outputState;

                    List<StateAndRef<AssetState>> states = getServiceHub().getVaultService().queryBy(AssetState.class).getStates();
                    boolean isError = true;
                    try {
                        states.stream().filter(artStateAndRef -> {
                                    AssetState assetState = artStateAndRef.getState().getData();
                                    return assetState.getLinearId().equals(state.getLinearId()) && assetState.getOwner().equals(state.getOwner());
                                }).findAny().orElseThrow(() -> new IllegalArgumentException("The piece of asset was not found."));
                        isError = false;
                    } catch (IllegalArgumentException err){
                    }
                    require.using("A similar record already exists", isError);
                    return null;
                });
            }
        }

        return subFlow(new SignTxFlow(counterpartySession, SignTransactionFlow.tracker()));
    }
}
