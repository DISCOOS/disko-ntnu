package org.redcross.sar.gui.event;

import java.util.EventListener;

import org.redcross.sar.gui.document.AutoCompleteDocument;

public interface IAutoCompleteListener extends EventListener {

	public void onSuggestionFound(AutoCompleteDocument document, String suggestion);
	
}
