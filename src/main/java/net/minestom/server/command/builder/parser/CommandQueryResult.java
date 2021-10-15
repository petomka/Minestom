package net.minestom.server.command.builder.parser;

import net.minestom.server.command.builder.Command;

public final class CommandQueryResult {
    private final Command command;
    private final String argsInput;
    private final String input;

    public CommandQueryResult(Command command, String argsInput, String input) {
        this.command = command;
        this.argsInput = argsInput;
        this.input = input;
    }

    public Command command() {
        return command;
    }

    public String argsInput() {
        return argsInput;
    }

    public String input() {
        return input;
    }
}
