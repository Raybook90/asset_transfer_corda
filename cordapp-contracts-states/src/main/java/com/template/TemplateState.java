package com.template;

import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;

import java.util.Arrays;
import java.util.List;

// *********
// * State *
// *********
public class TemplateState implements ContractState {

    private String name;
    private Party owner;

    public String getName() {
        return name;
    }

    public Party getOwner() {
        return owner;
    }

    public TemplateState(Party owner, String name) {
        this.owner = owner;
        this.name = name;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(owner);
    }
}