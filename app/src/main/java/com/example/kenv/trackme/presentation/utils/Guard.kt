package com.example.kenv.trackme.presentation.utils

/**
 * Created by Kenv on 31/12/2020.
 */
/**
 * Checks if the receiver is `null` and if so, executes the `nullClause`, forcing an early exit.
 * @param nullClause A block to be performed if receiver is null.
 * This block must end with a `return` statement, forcing an early exit from surrounding scope on `null`.
 * @return The receiver, now guaranteed not to be null.
 */
inline fun <T> T?.guard(nullClause: () -> Nothing): T {
    return this ?: nullClause()
}
