package me.chicchi7393.discogramRewrite.ticketHandlers

import it.tdlight.jni.TdApi.Chat
import it.tdlight.jni.TdApi.DownloadFile
import me.chicchi7393.discogramRewrite.JsonReader
import me.chicchi7393.discogramRewrite.discord.DsApp
import me.chicchi7393.discogramRewrite.mongoDB.DatabaseManager
import me.chicchi7393.discogramRewrite.objects.databaseObjects.TicketDocument
import me.chicchi7393.discogramRewrite.telegram.TgApp

class ticketHandler {
    private val settings = JsonReader().readJsonSettings("settings")!!
    private val dbMan = DatabaseManager.instance
    private val dsClass = DsApp.instance
    private val tgClient = TgApp.instance.client

    fun startTicketWithFile(id: Long, chat: Chat, file: DownloadFile?, text: String) {
        dsClass.dsClient.getCategoryById(
            settings.discord["category_id"] as Long
        )!!.createTextChannel(
            "${settings.discord["IDPrefix"]}${dbMan.Utils().getLastUsedTicketId() + 1}"
        ).map { it ->
            dbMan.Create().Tickets().createTicketDocument(
                TicketDocument(
                    id,
                    it.idLong,
                    dbMan.Utils().getLastUsedTicketId() + 1,
                    mapOf("open" to true, "suspended" to false, "closed" to false),
                    System.currentTimeMillis() / 1000
                )
            )
            dsClass.sendStartEmbed(
                chat,
                "Immagine",
                dbMan.Utils().getLastUsedTicketId() + 1,
                "https://discordapp.com/channels/${dsClass.getGuild().idLong}/${it.idLong}"
            )
            if (file == null) {
                dsClass.sendTextMessageToChannel(
                    dbMan.Utils().searchAlreadyOpen(id)!!.channelId, text
                ).queue()
            } else {
                tgClient.send(file) {
                    dsClass.sendTextMessageToChannel(
                        dbMan.Utils().searchAlreadyOpen(id)!!.channelId, text
                    )
                        .addFile(java.io.File(it.get().local.path)).queue()
                }
            }
        }.queue()
    }

    fun startTicketWithText(chat: Chat, text: String) = dsClass.createTicket(chat, text)
    fun sendFileFollowMessage(id: Long, file: DownloadFile?, text: String) {
        if (file == null) {
            dsClass.sendTextMessageToChannel(
                dbMan.Utils().searchAlreadyOpen(id)!!.channelId,
                text
            ).queue()
        } else {
            tgClient.send(file) {
                dsClass.sendTextMessageToChannel(
                    dbMan.Utils().searchAlreadyOpen(id)!!.channelId,
                    text
                )
                    .addFile(java.io.File(it.get().local.path)).queue()
            }
        }
    }

    fun sendTextFollowMessage(id: Long, text: String) =
        dsClass.sendTextMessageToChannel(dbMan.Utils().searchAlreadyOpen(id)!!.channelId, text).queue()
}