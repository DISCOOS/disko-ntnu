package org.disco.io;

public interface IResponse {
	
	public boolean isBoolean();
	public boolean isString();
	public boolean isBatch();
	public boolean isException();
	public boolean isArgumentCause();
	public boolean isUnexpected();
	
	public String getCommand();
	public Object getValue();
	
	public String getString();	
	public Boolean getBoolean();	
	public Throwable getException();
	public String getStackTrace();
	
	public String translate();

	public Object getSource();
	
	public void throwExecption() throws Throwable;
	
}
