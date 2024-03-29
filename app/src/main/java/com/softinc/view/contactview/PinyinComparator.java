package com.softinc.view.contactview;

import com.softinc.bean.Contact;

import java.util.Comparator;

/**
 * 
 * @author 
 *
 */
public class PinyinComparator implements Comparator<Contact> {

	public int compare(Contact o1, Contact o2) {
		if (o1.getSortLetters().equals("@")
				|| o2.getSortLetters().equals("#")) {
			return -1;
		} else if (o1.getSortLetters().equals("#")
				|| o2.getSortLetters().equals("@")) {
			return 1;
		} else {
			return o1.getSortLetters().compareTo(o2.getSortLetters());
		}
	}

}
