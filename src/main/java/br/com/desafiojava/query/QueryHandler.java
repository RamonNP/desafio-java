package br.com.desafiojava.query;

public interface QueryHandler<T, R> {
    R handle(T query);
}
