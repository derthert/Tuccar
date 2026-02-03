package poyrazinan.com.tr.tuccar.commands;

import poyrazinan.com.tr.tuccar.Tuccar;
import poyrazinan.com.tr.tuccar.commands.Main.MainCommands;

public class CommandRegister {
	
	public CommandRegister() {
		MainCommands mainCommands = new MainCommands(Tuccar.instance);
		Tuccar.instance.getCommand("tüccar").setExecutor(mainCommands);
		Tuccar.instance.getCommand("tüccar").setTabCompleter(mainCommands);
	}

}
