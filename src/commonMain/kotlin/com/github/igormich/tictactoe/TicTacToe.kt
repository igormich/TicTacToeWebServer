/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2010-2020 Igor Mikhailov aka https://github.com/igormich
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.github.igormich.tictactoe

import kotlinx.serialization.*
import kotlin.random.Random

enum class Marks(val code: Char) {
    NOUGHTS('O'),
    CROSSES('X'),
    EMPTY(' ');

    companion object {
        fun random() = if (Random.nextBoolean()) NOUGHTS else CROSSES
    }

    fun change(): Marks = when (this) {
        NOUGHTS -> CROSSES
        CROSSES -> NOUGHTS
        EMPTY -> throw IllegalStateException("Player cant be EMPTY")
    }
}

enum class Crossing {
    NONE,
    HORIZONTAL,
    VERTICAL,
    DIAGONAL_UP_DOWN,
    DIAGONAL_DOWN_UP,
}

const val GRID_SIZE = 3

@Serializable
sealed class Action

@Serializable
data class Turn(val x: Int, val y: Int, val mark: Marks)

val WRONG_TURN = Turn(-1, -1, Marks.EMPTY)

@Serializable
class TicTacToe(private var _activePlayer: Marks) {

    val activePlayer: Marks
        get() = _activePlayer

    var winner = Marks.EMPTY
    var turnCounter = 0
    val draw: Boolean
        get() = (winner == Marks.EMPTY) && turnCounter == 9

    @Serializable
    val grid = mutableListOf(
        mutableListOf(Marks.EMPTY, Marks.EMPTY, Marks.EMPTY),
        mutableListOf(Marks.EMPTY, Marks.EMPTY, Marks.EMPTY),
        mutableListOf(Marks.EMPTY, Marks.EMPTY, Marks.EMPTY)
    )

    @Serializable
    val crossing = mutableListOf(
        mutableListOf(Crossing.NONE, Crossing.NONE, Crossing.NONE),
        mutableListOf(Crossing.NONE, Crossing.NONE, Crossing.NONE),
        mutableListOf(Crossing.NONE, Crossing.NONE, Crossing.NONE)
    )

    private fun Turn.isLegal(): Boolean {
        return winner == Marks.EMPTY &&
                mark == _activePlayer &&
                x >= 0 && y >= 0 &&
                x < GRID_SIZE &&
                y < GRID_SIZE &&
                grid[x][y] == Marks.EMPTY
    }

    fun makeTurn(turn: Turn): Boolean {
        if (turn.isLegal()) {
            turnCounter++
            grid[turn.x][turn.y] = _activePlayer
            checkWinner()
            _activePlayer = _activePlayer.change()
            return true
        }
        return false
    }

    private fun checkWinner() {
        for ((i, line) in grid.withIndex()) {
            if (line.distinct().size == 1 && line[0] != Marks.EMPTY) {
                winner = line[0]
                crossing[i].fill(Crossing.HORIZONTAL)
                return
            }
        }
        for ((i, line) in grid.transpose().withIndex()) {
            if (line.distinct().size == 1 && line[0] != Marks.EMPTY) {
                winner = line[0]
                (0..2).forEach {
                    crossing[it][i] = Crossing.VERTICAL
                }
                return
            }
        }
        if (grid[0][0] != Marks.EMPTY && grid[0][0] == grid[1][1] && grid[1][1] == grid[2][2]) {
            crossing[0][0] = Crossing.DIAGONAL_UP_DOWN
            crossing[1][1] = Crossing.DIAGONAL_UP_DOWN
            crossing[2][2] = Crossing.DIAGONAL_UP_DOWN
            winner = grid[0][0]

        }
        if (grid[2][0] != Marks.EMPTY && grid[2][0] == grid[1][1] && grid[1][1] == grid[0][2]) {
            crossing[2][0] = Crossing.DIAGONAL_DOWN_UP
            crossing[1][1] = Crossing.DIAGONAL_DOWN_UP
            crossing[0][2] = Crossing.DIAGONAL_DOWN_UP
            winner = grid[1][1]
        }
    }
}

fun <E> List<List<E>>.transpose(): List<List<E>> {
    // Helpers
    fun <E> List<E>.head(): E = this.first()
    fun <E> List<E>.tail(): List<E> = this.takeLast(this.size - 1)
    fun <E> E.append(xs: List<E>): List<E> = listOf(this).plus(xs)

    this.filter { it.isNotEmpty() }.let { ys ->
        return if (ys.isNotEmpty())
            ys.map { it.head() }.append((ys.map { it.tail() }.transpose()))
        else
            emptyList()
    }
}



