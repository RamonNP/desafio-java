package br.com.desafiojava.command;

public interface CommandHandler<T> {
    void handle(T command);
}
