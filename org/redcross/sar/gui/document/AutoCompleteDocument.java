package org.redcross.sar.gui.document;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;
import javax.swing.text.AttributeSet;

import org.redcross.sar.gui.event.IAutoCompleteListener;

public class AutoCompleteDocument extends PlainDocument {

	private static final long serialVersionUID = 1L;

	private Object[] data;
	private boolean isStrict;
	private boolean isCaseSensitive;
	private JTextComponent inputField;

	public AutoCompleteDocument(Object[] data, JTextComponent inputField) {
		// forward
		this(data, inputField, false, false);
	}

	public AutoCompleteDocument(Object[] data, JTextComponent inputField, boolean isStrict, boolean isCaseSensitive) {
		// forward
		super();
		// prepare
		this.data = data;
		this.inputField = inputField;
		this.isStrict = isStrict;
		this.isCaseSensitive = isCaseSensitive;
	}
	
	@Override
	public void replace(int i, int j, String s, AttributeSet attributeset) throws BadLocationException {
		super.remove(i, j);
		insertString(i, s, attributeset);
	}

	@Override
	public void insertString(int i, String s, AttributeSet attributeset) throws BadLocationException {
		if (s == null || "".equals(s))
			return;
		String s1 = getText(0, i);
		String s2 = getMatch(s1 + s);
		int j = (i + s.length()) - 1;
		if (isStrict && s2 == null) {
			s2 = getMatch(s1);
			j--;
		} else if (!isStrict && s2 == null) {
			super.insertString(i, s, attributeset);
			return;
		}
		super.remove(0, getLength());
		super.insertString(0, s2, attributeset);
		inputField.setSelectionStart(j + 1);
		inputField.setSelectionEnd(getLength());
		// notify
		fireOnSuggestionFound(s2);
	}

	@Override
	public void remove(int i, int j) throws BadLocationException {
		int k = inputField.getSelectionStart();
		if (k > 0) k--;
		String s = getMatch(getText(0, k));
		if (!isStrict && s == null) {
			super.remove(i, j);
		} else {
			super.remove(0, getLength());
			super.insertString(0, s, null);
		}
		inputField.setSelectionStart(k);
		inputField.setSelectionEnd(getLength());
		fireOnSuggestionFound(s);
	}

	public boolean isCaseSensitive() {
		return isCaseSensitive;
	}

	public void setCaseSensitive(boolean flag) {
		isCaseSensitive = flag;
	}

	public boolean isStrict() {
		return isStrict;
	}

	public void setStrict(boolean flag) {
		isStrict = flag;
	}

	public Object[] getData() {
		return data;
	}

	public void setData(Object[] data) {
		this.data = data;
	}	

	public void addAutoCompleteListener(IAutoCompleteListener listener) {
		listenerList.add(IAutoCompleteListener.class, listener);
	}

	public void removeAutoCompleteListener(IAutoCompleteListener listener) {
		listenerList.remove(IAutoCompleteListener.class, listener);
	}

	private void fireOnSuggestionFound(String suggestion) {
		IAutoCompleteListener[] list = listenerList.getListeners(IAutoCompleteListener.class);
		for(IAutoCompleteListener it : list) {
			it.onSuggestionFound(this, suggestion);
		}
	}

	private String getMatch(String s) {
		if(data!=null && s!=null && !s.isEmpty()) {
			for (Object it : data) {
				if (it != null) {
					String text = isCaseSensitive?it.toString():it.toString().toLowerCase();
					if (text.startsWith(s.toLowerCase())) {
						return it.toString();
					}
				}
			}
		}
		return null;
	}
}	