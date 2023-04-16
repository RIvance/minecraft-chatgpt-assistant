package org.ivance.gptassistant.util

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.Vec3d
import kotlin.math.pow
import kotlin.math.roundToInt

fun Double.round(precision: Int): Double {
    val factor = 10.0.pow(precision)
    return (this * factor).roundToInt() / factor
}

fun Vec3d.toTupleString(precision: Int = 2): String {
    return "(${this.x.round(precision)}, ${this.y.round(precision)}, ${this.z.round(precision)})"
}

fun PlayerEntity.format(string: String): String {
    return string
        .replace("\$name", this.name.string)
        .replace("\$world", this.world.toString())
        .replace("\$pos", this.pos.toTupleString())
        .replace("\$rotation", this.rotationVector.toTupleString())
}
