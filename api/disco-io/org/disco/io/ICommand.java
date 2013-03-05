package org.disco.io;

import java.util.Collection;

public interface ICommand {

	public boolean isSingleWord();

	public String getName();

	public String build();
	public String getCommand();

	public void add(String arg, String flag);
	
	public void remove(String arg);
	
	public Collection<String> getArgs();
	
	public int size();
	
	public Collection<String> getFlags();
	
	public String getFlag(String arg);

	public boolean argExists(String arg);
	
	public String[] getFlags(String[] args);
	
	public boolean contains(String arg);
	
	public boolean containsAll(String[] args);

	public int containsSome(String[] args);
	
	public boolean equals(String name);
	
}
