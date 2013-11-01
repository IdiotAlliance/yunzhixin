package com.dt.cloudmsg.model;

import java.util.ArrayList;
import java.util.List;

public class ContactsEntity {

	private static final String TAG = ContactsEntity.class.getSimpleName();

    private String contact_ID;

    private String phone_ID;

    private String display_name;

    private List<String> number = new ArrayList<String>();

    public ContactsEntity() {
    }

    public ContactsEntity(String contact_ID, String phone_ID, String display_name, String number) {
        super();
        this.contact_ID = contact_ID;
        this.phone_ID = phone_ID;
        this.display_name = display_name;
        this.number.add(number);
    }


    public String getContactID() {
        return contact_ID;
    }

    public void setContactID(String contact_ID) {
        this.contact_ID = contact_ID;
    }

    public String getPhoneID() {
        return phone_ID;
    }

    public void setPhoneID(String phone_ID) {
        this.phone_ID = phone_ID;
    }

    public String getDisplayName() {
        return display_name;
    }

    public void setDisplayName(String display_name) {
        this.display_name = display_name;
    }

    public List<String> getNumber() {
        return number;
    }

    public void setNumber(List<String> number) {
        this.number = number;
    }

    public void addNumber(String number){
        this.number.add(number);
    }
    
}
