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

package com.github.igormich.chess.com.github.igormich.tictactoe

@Suppress("MayBeConstant")
object Strings {
    val LEAVE: String = "Ваш соперник вышел, ищем другого"
    val WAIT: String = "Ожидание соперника"
    val DRAW: String = "Ничья. Новая игра скоро начнётся"
    val WIN: String = "Вы победили! Новая игра скоро начнётся"
    val LOSE: String = "Вы проиграли, но скоро начнётся новая игра "
    fun youPlayAs(code: Char) = "Вы играете за $code."
    val YOU_TURN = "Сейчас ваш ход"
    val ENEMY_TURN = "Сейчас ход противника"
}