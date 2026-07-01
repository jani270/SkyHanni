package at.hannibal2.skyhanni.events.entity

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.events.entity.abstract.SkyHanniEntityEvent
import at.hannibal2.skyhanni.skyhannimodule.PrimaryFunction
import net.minecraft.network.chat.Component


@PrimaryFunction("onEntityHealthDisplay")
class EntityHealthDisplayEvent(
    var text: Component,
) : SkyHanniEvent()
