package com.asset.webserver.Objects;

public class Asset {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;

    public PartyInfo getOwner() {
        return owner;
    }

    public void setOwner(PartyInfo owner) {
        this.owner = owner;
    }

    public PartyInfo getValidator() {
        return validator;
    }

    public void setValidator(PartyInfo validator) {
        this.validator = validator;
    }

    private PartyInfo owner, validator;
}