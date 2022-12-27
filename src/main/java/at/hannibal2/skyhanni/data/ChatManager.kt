package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.events.LorenzActionBarEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.PacketEvent
import at.hannibal2.skyhanni.utils.LorenzLogger
import at.hannibal2.skyhanni.utils.LorenzUtils
import net.minecraft.network.play.server.S02PacketChat
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class ChatManager {

    private val loggerAll = LorenzLogger("chat/all")
    private val loggerFiltered = LorenzLogger("chat/blocked")
    private val loggerAllowed = LorenzLogger("chat/allowed")
    private val loggerModified = LorenzLogger("chat/modified")
    private val loggerFilteredTypes = mutableMapOf<String, LorenzLogger>()

    @SubscribeEvent(priority = EventPriority.LOW, receiveCanceled = true)
    fun onActionBarPacket(event: PacketEvent.ReceiveEvent) {
        val packet = event.packet
        if (packet !is S02PacketChat) return
        val messageComponent = packet.chatComponent

        val message = LorenzUtils.stripVanillaMessage(messageComponent.formattedText)
        if (packet.type.toInt() == 2) {
            val actionBarEvent = LorenzActionBarEvent(message)
            actionBarEvent.postAndCatch()
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    fun onChatReceive(event: ClientChatReceivedEvent) {
        if (event.type.toInt() == 2) return

        val original = event.message
        val message = LorenzUtils.stripVanillaMessage(original.formattedText)

        if (message.startsWith("§f{\"server\":\"")) return

        val chatEvent = LorenzChatEvent(message, original)
        chatEvent.postAndCatch()

        val blockReason = chatEvent.blockedReason.uppercase()
        if (blockReason != "") {
            event.isCanceled = true
            loggerFiltered.log("[$blockReason] $message")
            loggerAll.log("[$blockReason] $message")
            loggerFilteredTypes.getOrPut(blockReason) { LorenzLogger("chat/filter_blocked/$blockReason") }
                .log(message)
            return
        }

        val modified = chatEvent.chatComponent
        if (modified.formattedText == original.formattedText) {
            loggerAllowed.log(message)
            loggerAll.log("[allowed] $message")
        } else {
            event.message = chatEvent.chatComponent
            loggerModified.log(" ")
            loggerModified.log("[original] " + original.formattedText)
            loggerModified.log("[modified] " + modified.formattedText)
            loggerAll.log("[modified] " + modified.formattedText)
        }
    }
}