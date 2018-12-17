package com.asset;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.utilities.ProgressTracker;

import static net.corda.core.contracts.ContractsDSL.requireThat;

// ******************
// * Responder flow *
// ******************
@InitiatedBy(Initiator.class)
public class Responder extends FlowLogic<Void> {
    private FlowSession counterpartySession;

    public Responder(FlowSession counterpartySession) {
        this.counterpartySession = counterpartySession;
    }

    @Suspendable
    @Override
    public Void call() throws FlowException {
        // Responder flow logic goes here.
        // getServiceHub().loadState(stx.tx.inputs.toSet());
        class SignTxFlow extends SignTransactionFlow {
            private SignTxFlow(FlowSession otherSession, ProgressTracker progressTracker) {
                super(otherSession, progressTracker);
            }

            @Override
            protected void checkTransaction(SignedTransaction stx) {
                requireThat(require -> {
                    // Any additional checking we see fit...
                    AssetState outputState = (AssetState) stx.getTx().getOutputs().get(0).getData();
                    assert (outputState.getName() == "House");
                    return null;
                });
            }
        }

        subFlow(new SignTxFlow(counterpartySession, SignTransactionFlow.tracker()));
        return null;
    }
}
