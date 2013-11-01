package com.dt.cloudmsg.datasource;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.dt.cloudmsg.model.ContactsEntity;
import com.dt.cloudmsg.service.MyService;
import com.dt.cloudmsg.util.NumberFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactsSource extends AbstractDataSource<ContactsEntity>{

    private static List<ContactsEntity> contacts = new ArrayList<ContactsEntity>();
    private static Map<String, String> numNameMap = new HashMap<String, String>();

	public ContactsSource(Context context) {
		super(context, null);
        load();
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return contacts.size();
	}

	@Override
	public ContactsEntity get(int index) {
		// TODO Auto-generated method stub
		return contacts.get(index);
	}
	
	public String getNameByNumber(String number){
		return numNameMap.get(number);
	}

	@Override
	public void registerDataChangeListener(XDataChangeListener<ContactsEntity> listener) {
		// TODO Auto-generated method stub
		super.registerDataChangeListener(listener);
	}

	@Override
	public void removeListener(XDataChangeListener<ContactsEntity> listener) {
		// TODO Auto-generated method stub
		super.removeListener(listener);
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		super.close();
	}


    @Override
    public void onChange() {
        load();
        notifyChange();
    }

    @Override
    public void onUpdate(ContactsEntity contactsEntity) {
        notifyUpdate(contactsEntity);
    }

    @Override
    public void onAdd(ContactsEntity contactsEntity) {
        notifyAdd(contactsEntity);
    }

    @Override
    public void onDelete(ContactsEntity contactsEntity) {
        notifyDelete(contactsEntity);
    }

    @Override
    protected void load() {
        // clear data
        contacts.clear();
        numNameMap.clear();

        ContentResolver cr = context.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,null, null, null, null);

        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                ContactsEntity entity = new ContactsEntity();
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                entity.setContactID(id);
                entity.setDisplayName(name);

                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    // System.out.println("name : " + name + ", ID : " + id);

                    // get the phone number
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phone = pCur.getString(
                                pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        // System.out.println("phone" + phone);
                        entity.addNumber(phone);
                        numNameMap.put(NumberFormatter.normalizeNumber(phone), name);
                    }
                    pCur.close();
                }
                contacts.add(entity);
            }
        }
        notifyChange();
    }

    public String getDisplayName(String number){
        return numNameMap.get(NumberFormatter.normalizeNumber(number));
    }
}
