package com.example.transactionkeyvaluestore

import android.os.Bundle
import android.text.Editable
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged

class MainActivity : AppCompatActivity() {

    private var inputHistory: TextView? = null
    private var inputText: EditText? = null
    private val transactionsStack = Stack<HashMap<String, String>>()
    private val rootKeyValueStore = hashMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initUi()
    }

    private fun initUi() {
        inputHistory = findViewById(R.id.inputHistory)
        inputText = findViewById<EditText?>(R.id.input)?.apply {
            setText(inputPrefix)
            doAfterTextChanged(::handlePrefixForInput)
            setOnEditorActionListener(::handleGoActionForInput)
            requestFocus()
        }
    }

    private fun handlePrefixForInput(text: Editable?) {
        if (text?.startsWith(inputPrefix) == false) {
            val textWithPrefix = "$inputPrefix$text"
            inputText?.setText(textWithPrefix)
            inputText?.setSelection(inputText?.text?.length ?: 0)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun handleGoActionForInput(
        textView: TextView,
        actionId: Int,
        event: KeyEvent
    ): Boolean {
        if (actionId == EditorInfo.IME_ACTION_GO) {
            handleInput()
            inputText?.setText(inputPrefix)
            inputText?.setSelection(inputText?.text?.length ?: 0)
            return true
        }
        return false
    }

    private fun handleInput() {
        val history: String = inputHistory?.text?.toString().orEmpty()
        var input: String = inputText?.text?.toString().orEmpty()
        val inputWithoutPrefix = input.removePrefix(inputPrefix)
        val inputPhrase = inputWithoutPrefix.split(" ")
        input = when {
            inputWithoutPrefix containsCommand SET -> if (inputPhrase.size == 3) {
                val key = inputPhrase[1]
                val value = inputPhrase[2]
                getTopTransaction()?.set(key, value)
                "$input\n$added"
            } else "$input\n$commandSyntaxError"

            inputWithoutPrefix containsCommand GET -> if (inputPhrase.size == 2) {
                val key = inputPhrase[1]
                val valueToHistory = getTopTransaction()?.get(key).orEmpty()
                if (valueToHistory.isBlank()) "$input\n$keyNotSetError"
                else "$input\n$valueToHistory"
            } else "$input\n$commandSyntaxError"

            inputWithoutPrefix containsCommand DELETE -> if (inputPhrase.size == 2) {
                val key = inputPhrase[1]
                val valueToHistory = getTopTransaction()?.remove(key).orEmpty()
                if (valueToHistory.isBlank()) "$input\n$noSuchValueError"
                else "$input\n$deleted \"$key $valueToHistory\""
            } else "$input\n$commandSyntaxError"

            inputWithoutPrefix containsCommand COUNT -> if (inputPhrase.size == 2) {
                val valueToFind = inputPhrase[1]
                val valuesCountToHistory = getTopTransaction()
                    ?.filter { it.value == valueToFind }
                    ?.size ?: 0
                if (valuesCountToHistory == 0) "$input\n$noValuesError"
                else "$input\n$valuesCountToHistory"
            } else "$input\n$commandSyntaxError"

            inputWithoutPrefix containsCommand BEGIN -> {
                transactionsStack.push(hashMapOf())
                input
            }
            inputWithoutPrefix containsCommand COMMIT ->
                if (transactionsStack.isEmpty()) "$input\n$noTransactionError"
                else {
                    val topTransaction = transactionsStack.peek()
                    transactionsStack.pop()
                    topTransaction?.forEach { (key, value) ->
                        if (transactionsStack.isEmpty()) rootKeyValueStore[key] = value
                        else transactionsStack.peek()?.set(key, value)
                    }
                    "$input\n$transactionCommitted"
                }
            inputWithoutPrefix containsCommand ROLLBACK ->
                if (transactionsStack.isEmpty()) "$input\n$noTransactionError"
                else {
                    transactionsStack.pop()
                    "$input\n$transactionRemoved"
                }
            else -> "$input\n$commandSyntaxError"
        }

        val newHistoryText = when {
            history.isBlank() && input.isNotBlank() -> input
            history.isNotBlank() && input.isNotBlank() -> history + "\n\n" + input
            else -> history
        }
        inputHistory?.text = newHistoryText
    }

    private fun getTopTransaction() =
        if (transactionsStack.isEmpty()) rootKeyValueStore
        else transactionsStack.peek()

    private infix fun String.containsCommand(command: String) =
        startsWith(prefix = command, ignoreCase = true)

    private companion object {
        const val inputPrefix = "> "
        // errors
        const val commandSyntaxError = "incorrect command"
        const val keyNotSetError = "key not set"
        const val noSuchValueError = "value does not exist or empty"
        const val noValuesError = "no values found"
        const val noTransactionError = "no transaction"

        // success messages
        const val deleted = "successfully deleted"
        const val added = "successfully added"
        const val transactionRemoved = "last transaction removed"
        const val transactionCommitted = "last transaction committed"

        // commands
        const val DELETE = "DELETE "
        const val GET = "GET "
        const val SET = "SET "
        const val COUNT = "COUNT "
        const val COMMIT = "COMMIT"
        const val BEGIN = "BEGIN"
        const val ROLLBACK = "ROLLBACK"
    }
}