package org.redcross.sar.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.redcross.sar.gui.field.IDiskoField;

public class DiskoFieldEdit extends AbstractUndoableEdit {
	
	private static final long serialVersionUID = 1L;

	private Object m_undoValue;
	private Object m_redoValue;
	private IDiskoField m_field;
	
	public DiskoFieldEdit(IDiskoField field, Object undoValue, Object redoValue) {
		// prepare
		m_field = field;
		m_undoValue = undoValue;
		m_redoValue = redoValue;
	}

	@Override
	public String getPresentationName() {
		return m_field.getCaptionText();
	}

	@Override
	public void redo() throws CannotRedoException {
		// forward
		super.redo();
		// redo
		m_field.setValue(m_redoValue);
	}

	@Override
	public void undo() throws CannotUndoException {
		// forward
		super.undo();
		// undo
		m_field.setValue(m_undoValue);
	}
	
	
}
