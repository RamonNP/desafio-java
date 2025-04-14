package br.com.desafiojava.query;

import java.util.Optional;

public interface QueryHandler<T, R> {
    R handle(T query);
}
