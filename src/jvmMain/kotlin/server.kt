package com.github.igormich.tictactoe

import com.github.igormich.tictactoe.Marks
import com.github.igormich.tictactoe.Wait
import com.github.igormich.tictactoe.toJson
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.http.content.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.html.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap

fun HTML.index() {
    head {
        title("Hello from Ktor!")
        styleLink("/static/style.css")
    }
    body {
        div {
            classes = setOf("center")
            id = "root"
        }
        script(src = "/static/js.js") {}
    }
}

class Player(val session: WebSocketSession, var mark: Marks = Marks.EMPTY, val id: UUID = UUID.randomUUID()) :
    WebSocketSession by session {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Player) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

val gameSessions: MutableMap<Player, GameSession> = ConcurrentHashMap()
val awaitingPlayers = Channel<Player>()


fun main() {
    /*var data = format.encodeToString<GameEvent>(PolymorphicSerializer(GameEvent::class), Wait())
    println(data)
    val wait = format.decodeFromString<GameEvent>(PolymorphicSerializer(GameEvent::class), data)
    println(wait)
    data =
        format.encodeToString<GameEvent>(PolymorphicSerializer(GameEvent::class), DeskChange(TicTacToe(Marks.random())))
    println(data)
    val newGame = format.decodeFromString<GameEvent>(PolymorphicSerializer(GameEvent::class), data)
    println(newGame)*/
    runBlocking {
        launch(Dispatchers.Default) {
            while (true) {
                try {
                    println("Wait player1")
                    val player1 = awaitingPlayers.receive()
                    player1.session.send(Wait().toJson())
                    val player2 = awaitingPlayers.receive()
                    val gameSession = GameSession(player1, player2)
                    gameSessions[player1] = gameSession
                    gameSessions[player2] = gameSession
                    gameSession.start()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        launch {
            embeddedServer(Netty, port = 8080) {
                install(WebSockets) {
                }
                routing {
                    get("/") {
                        call.respondHtml(HttpStatusCode.OK, HTML::index)
                    }
                    file("/static/js.js", "./build/distributions/js.js")
                    file("/static/js.js.map", "./build/distributions/js.js.map")
                    file("/static/style.css", "./style.css")
                    webSocket("/tictactoe", null) {
                        println("New ws")
                        try {
                            val player = Player(this)
                            awaitingPlayers.send(player)
                            val a = this.closeReason.await()
                            println(a)
                            gameSessions[player]?.stop(this)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }.start(wait = true)
        }
    }
}
