package com.example.android.models;
/**
 * Emergency contact model
 */
public class EmergencyContact {

    private String name;
    private String phoneNumber;
    private String relationship;

    public EmergencyContact() {
    }

    public EmergencyContact(String name, String phoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.relationship = "Emergency Contact";
    }

    public EmergencyContact(String name, String phoneNumber, String relationship) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.relationship = relationship;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }
}
