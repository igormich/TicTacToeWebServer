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

import com.github.igormich.chess.com.github.igormich.tictactoe.Strings
import com.github.igormich.tictactoe.Crossing.*
import kotlinx.browser.window
import kotlinx.html.js.onClickFunction
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.w3c.dom.MessageEvent
import react.*
import react.dom.*

private val Crossing.asCssClass
    get() = when (this) {
        NONE -> ""
        HORIZONTAL -> "line-horizontal"
        VERTICAL -> "line-vertical"
        DIAGONAL_UP_DOWN -> "line-diag-up-down"
        DIAGONAL_DOWN_UP -> "line-diag-down-up"
    }

external interface TicTacToeProps : RProps {
    var desk: TicTacToe?
}

data class TicTacToeState(
    var desk: TicTacToe? = null,
    var myMark: Marks = Marks.EMPTY,
    var wrongMove: Turn = WRONG_TURN,
    var disconnect: Boolean = false
) : RState

fun TicTacToeComponent.onMessage(message: MessageEvent) {
    val data = message.data
    if (data is String) {
        console.log(data)
        when (val gameEvent = format.decodeFromString(PolymorphicSerializer(GameEvent::class), data)) {
            is DeskChange -> setState {
                desk = gameEvent.desk
            }
            is Disconnect -> setState {
                disconnect = true
                desk = null
            }
            is Wait -> setState {
                desk = null
            }
            is NewGame -> setState {
                disconnect = false
                desk = gameEvent.desk
                myMark = gameEvent.mark
            }
        }
    }
}

class TicTacToeComponent(props: TicTacToeProps) : RComponent<TicTacToeProps, TicTacToeState>(props) {
    init {
        state = TicTacToeState(props.desk)
        webSocket.onmessage = ::onMessage
    }

    override fun RBuilder.render() {
        val desk = state.desk
        if (desk == null) {
            div {
                +if (state.disconnect) {
                    Strings.LEAVE
                } else
                    Strings.WAIT
            }
        } else {

            div {

                +when {
                    desk.draw -> Strings.DRAW
                    desk.winner == state.myMark -> Strings.WIN
                    desk.winner == state.myMark.change() -> Strings.LOSE
                    else -> Strings.youPlayAs(state.myMark.code) + " " + if (desk.activePlayer == state.myMark)
                        Strings.YOU_TURN
                    else
                        Strings.ENEMY_TURN
                }

            }
            table(classes = "desk") {
                for ((x, line) in desk.grid.withIndex())
                    tr {
                        for ((y, cell) in line.withIndex()) {
                            val classes =
                                desk.crossing[x][y].asCssClass + if ((state.wrongMove.x == x) && (state.wrongMove.y == y)) ", red" else ""
                            td(classes = classes) {
                                attrs {
                                    onClickFunction = {
                                        val turn = Turn(x, y, state.myMark)
                                        if (desk.makeTurn(turn)) {
                                            webSocket.send(Json.encodeToString(turn))
                                            setState {
                                                this.desk = desk
                                            }
                                        } else {
                                            setState {
                                                wrongMove = Turn(x, y, Marks.EMPTY)
                                            }
                                            window.setTimeout({
                                                setState { wrongMove = WRONG_TURN }
                                            }, 1000)
                                        }

                                    }

                                }
                                +cell.code.toString()
                            }
                        }
                    }
            }
        }
    }
}

