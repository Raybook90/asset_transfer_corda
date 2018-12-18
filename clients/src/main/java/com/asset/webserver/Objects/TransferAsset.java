package com.asset.webserver.Objects;

public class TransferAsset {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;

    public PartyInfo getNewOwner() {
        return newOwner;
    }

    public void setNewOwner(PartyInfo newOwner) {
        this.newOwner = newOwner;
    }

    private PartyInfo newOwner;
}