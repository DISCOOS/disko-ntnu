package org.disco.io;

public class CommandParser {

	String token;
	
	CommandParser(String token) {
		this.token = token;
	}
	
	public Command parse(String command) {

		int i = 0;
		String arg = null;
		Command cmd = null;

		// split command on space
		String[] args  = command.split("\\s");
		
		// single work command?
		if(args.length==1 && (args[0]==null || args[0].isEmpty())) {
			// create command
			cmd = new Command(command,command);				
		} else {
		
			// create command
			cmd = new Command(args[0],command);

			// loop over all arguments
			for(i=1;i<args.length;i++) {
				
				// is argument?
				if(args[i].startsWith(token)) {
					// store argument
					arg = args[i].substring(1);
				}
				else if(arg!=null){
					// add to command
					cmd.add(arg, args[i]);
					// reset argument
					arg = null;
				}
				else if(arg==null && i==1) {
					// add to command
					cmd.add(args[i], "");
				}
	
			}
			/*
			if (i == args.length)
				System.err.println("Usage: ParseCmdLine [-verbose] [-xn] [-output afile] filename");
			else
				System.out.println("Success!");			
			*/
		}
		
		return cmd;
	}

}	