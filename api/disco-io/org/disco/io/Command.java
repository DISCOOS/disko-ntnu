package org.disco.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Command implements ICommand {

	String name;	
	String command;
	Map<String,String> args = new HashMap<String, String>();

	Command(String name, String command) {
		this.name = name;
		this.command = command;
	}
	
	public boolean isSingleWord() {
		return name.equalsIgnoreCase(getCommand());
	}
	
	public String getName() {
		return name;
	}

	public String getCommand() {
		return command;
	}
		
	public String build() {
		String cmd = name;
		for(String arg : args.keySet()) {
			cmd = cmd.concat(" -" + arg + " " + args.get(arg));
		}
		return cmd;
	}

	public void add(String arg, String flag) {
		args.put(arg.toLowerCase(), flag);
	}
	
	public void remove(String arg) {
		args.remove(arg);
	}

	public Collection<String> getArgs() {
		return args.keySet();
	}
		
	public String getFlag(String arg) {
		return args.get(arg);
	}

	public boolean argExists(String arg) {
		return arg.contains(arg);
	}
	
	public Collection<String> getFlags() {
		return args.values();
	}
	
	public String[] getFlags(String[] args) {
		String[] flags = new String[args.length];
		for(int i = 0;i<args.length;i++) {
			flags[i] = getFlag(args[i]);
			
		}
		return flags;
	}
	
	public int size() {
		return args.size();
	}
	
	public int containsSome(String[] args) {
		int count = 0;
		for(String it : args) {
			if(contains(it)) count++;
		}
		return count;
	}
	
	
	public boolean contains(String arg) {
		return this.args.keySet().contains(arg);		
	}
	
	public boolean containsAll(String[] args) {
		return this.args.keySet().containsAll(getList(args));
	}

	private List<String> getList(String[] items) {
		List<String> list = new ArrayList<String>(items.length);
		for(String it : items) 
			list.add(it);
		return list;
	}		
	
	public boolean equals(String name) {
		return this.name.equalsIgnoreCase(name);
	}

}
