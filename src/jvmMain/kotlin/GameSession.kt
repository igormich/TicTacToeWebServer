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

import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

data class GameSession(val player1: Player, val player2: Player) {

    private lateinit var desk: TicTacToe

    private lateinit var job1: Job
    private lateinit var job2: Job

    private fun newGame() {
        player1.mark = Marks.random()
        player2.mark = player1.mark.change()
        desk = TicTacToe(Marks.random())
    }

    suspend fun start() = coroutineScope {
        newGame()
        player1.send(NewGame(desk, player1.mark).toJson())
        player2.send(NewGame(desk, player2.mark).toJson())
        job1 = launch {
            listenPlayer(player1, player2)
        }
        job2 = launch {
            listenPlayer(player2, player1)
        }
    }


    private suspend fun listenPlayer(player1: Player, player2: Player) = coroutineScope {
        try {
            player1.incoming.receiveAsFlow().filterIsInstance<Frame.Text>()
                .map { it.readText() }
                .filter { it.isNotEmpty() }
                .collect {
                    val turn = Json.decodeFromString<Turn>(it)
                    if (turn.mark == player1.mark && desk.makeTurn(turn)) {
                        player2.send(DeskChange(desk).toJson())
                        if (desk.winner != Marks.EMPTY || desk.draw) {
                            launch(Dispatchers.Default) {
                                restart()
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun restart() {
        delay(5000)
        desk = TicTacToe(Marks.random())
        player1.mark = Marks.random()
        player2.mark = player1.mark.change()
        player1.send(NewGame(desk, player1.mark).toJson())
        player2.send(NewGame(desk, player2.mark).toJson())
    }

    suspend fun stop(leaver: WebSocketSession) {
        gameSessions.remove(player1)
        gameSessions.remove(player2)
        job1.cancel()
        job2.cancel()
        try {
            if (player1.session == leaver) {
                player2.send(Disconnect().toJson())
                awaitingPlayers.send(player2)
            } else {
                player1.send(Disconnect().toJson())
                awaitingPlayers.send(player1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
