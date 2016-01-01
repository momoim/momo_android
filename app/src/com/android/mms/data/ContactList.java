
package com.android.mms.data;

import java.util.ArrayList;
import java.util.Collection;

import android.text.TextUtils;

public class ContactList extends ArrayList<Contact> {

    /**
     * 
     */
    private static final long serialVersionUID = 8985588641301264292L;

    /**
     * Returns a ContactList for the corresponding recipient ids passed in. This
     * method will create the contact if it doesn't exist, and would inject the
     * recipient id into the contact.
     */
    public static ContactList getByIds(String spaceSepIds, boolean canBlock) {
        ContactList list = new ContactList();
        for (RecipientIdCache.Entry entry : RecipientIdCache.getAddresses(spaceSepIds)) {
            if (entry != null && !TextUtils.isEmpty(entry.number)) {
                Contact contact = Contact.get(entry.number);
                contact.setRecipientId(entry.id);
                list.add(contact);
            }
        }
        return list;
    }

    @Override
    public boolean add(Contact contact) {
        // TODO Auto-generated method stub
        return super.add(contact);
    }

    @Override
    public void add(int index, Contact object) {
        // TODO Auto-generated method stub
        super.add(index, object);
    }

    @Override
    public boolean addAll(Collection<? extends Contact> collection) {
        // TODO Auto-generated method stub
        return super.addAll(collection);
    }

    @Override
    public boolean addAll(int location, Collection<? extends Contact> collection) {
        // TODO Auto-generated method stub
        return super.addAll(location, collection);
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub
        super.clear();
    }

    @Override
    public Object clone() {
        // TODO Auto-generated method stub
        return super.clone();
    }

    @Override
    public boolean contains(Object object) {
        // TODO Auto-generated method stub
        return super.contains(object);
    }

    @Override
    public void ensureCapacity(int minimumCapacity) {
        // TODO Auto-generated method stub
        super.ensureCapacity(minimumCapacity);
    }

    @Override
    public Contact get(int index) {
        // TODO Auto-generated method stub
        return super.get(index);
    }

    @Override
    public int indexOf(Object object) {
        // TODO Auto-generated method stub
        return super.indexOf(object);
    }

    @Override
    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return super.isEmpty();
    }

    @Override
    public int lastIndexOf(Object object) {
        // TODO Auto-generated method stub
        return super.lastIndexOf(object);
    }

    @Override
    public Contact remove(int index) {
        // TODO Auto-generated method stub
        return super.remove(index);
    }

    @Override
    public boolean remove(Object object) {
        // TODO Auto-generated method stub
        return super.remove(object);
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        // TODO Auto-generated method stub
        super.removeRange(fromIndex, toIndex);
    }

    @Override
    public Contact set(int index, Contact object) {
        // TODO Auto-generated method stub
        return super.set(index, object);
    }

    @Override
    public int size() {
        // TODO Auto-generated method stub
        return super.size();
    }

    @Override
    public Object[] toArray() {
        // TODO Auto-generated method stub
        return super.toArray();
    }

    @Override
    public <T> T[] toArray(T[] contents) {
        // TODO Auto-generated method stub
        return super.toArray(contents);
    }

    @Override
    public void trimToSize() {
        // TODO Auto-generated method stub
        super.trimToSize();
    }

}
