package com.evacipated.cardcrawl.modthespire;

@FunctionalInterface
interface TriConsumer<T, U, V>
{
    void accept(T t, U u, V v);
}
