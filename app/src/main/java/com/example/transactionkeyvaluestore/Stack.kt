package com.example.transactionkeyvaluestore

class Stack<T> {
    
    private val elements: MutableList<T> = mutableListOf()

    fun isEmpty() = elements.isEmpty()

    fun push(item: T) = elements.add(item)

    fun pop() : T? {
        val item = elements.lastOrNull()
        if (!isEmpty()){
            elements.removeAt(elements.size -1)
        }
        return item
    }
    fun peek() : T? = elements.lastOrNull()

    override fun toString(): String = elements.toString()
}