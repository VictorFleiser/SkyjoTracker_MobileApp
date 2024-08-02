package com.example.skyjocounter;

import java.io.Serializable;

public class Tuple<A, B> implements Serializable {
    private static final long serialVersionUID = 1L;
    public final A first;
    public final B second;

    public Tuple(A first, B second) {
        this.first = first;
        this.second = second;
    }
}
