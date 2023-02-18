package cc.zork.features.ui

import com.google.common.collect.Iterables
import com.google.common.collect.Lists
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.scoreboard.ScoreObjective
import net.minecraft.scoreboard.ScorePlayerTeam
import net.minecraft.util.EnumChatFormatting
import cc.zork.event.Shader2DEvent
import cc.zork.event.Subscribe
import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.features.ui.HUD.Companion.arraylist
import cc.zork.ui.font.FontManager
import cc.zork.utils.render.RoundedUtil
import java.awt.Color
import java.util.regex.Pattern
import java.util.stream.Collectors
import kotlin.math.max


class Scoreboard : Module("Scoreboard", "New Look of the scoreboard", ModuleType.Interface) {
    companion object {
        var downY = 0
        var maxHeight = 0
        var left = 0
        fun renderScoreboard(objective: ScoreObjective, scaledRes: ScaledResolution) {
            val scoreboard = objective.scoreboard
            var collection = scoreboard.getSortedScores(objective)
            val list = collection.stream()
                .filter { s -> s != null && !s.playerName.startsWith("#") }
                .collect(Collectors.toList())

            collection = if (list.size > 15) {
                Lists.newArrayList(Iterables.skip(list, collection.size - 15))
            } else {
                list
            }

            val fontRenderer = FontManager.small
            var maxWidth = fontRenderer.getStringWidth(objective.displayName)
            for (score in collection) {
                val scoreplayerteam = scoreboard.getPlayersTeam(score.playerName)
                val s = ScorePlayerTeam.formatPlayerName(
                    scoreplayerteam,
                    score.playerName
                ) + ": " + EnumChatFormatting.RED + score.scorePoints
                maxWidth = max(maxWidth, fontRenderer.getStringWidth(s))
            }
            maxHeight = (collection.size + 1) * fontRenderer.FONT_HEIGHT + 4
            downY =
                max(arraylist.height + maxHeight + 24, (scaledRes.scaledHeight / 2f + maxHeight / 3f + 30).toDouble())
                    .toInt()
            left = scaledRes.scaledWidth - maxWidth - 5
            val right = scaledRes.scaledWidth - 5
            var scoreCounter = 0
            RoundedUtil.drawRound(
                left - 2f,
                downY - maxHeight - 1f,
                right - left + 2f,
                downY + 2f - (downY - maxHeight - 1f),
                2f,
                Color(0, 0, 0, 60)
            )
            for (score1 in collection) {
                ++scoreCounter
                var playerName =
                    ScorePlayerTeam.formatPlayerName(scoreboard.getPlayersTeam(score1.playerName), score1.playerName)
                val matcher = Pattern.compile("[\uD83C-\uDBFF\uDC00-\uDFFF]+").matcher(playerName)
                if (matcher.find()) {
                    playerName = matcher.replaceAll("")
                }
                playerName = playerName.replace("⚽".toRegex(), "").replace("hypixel","haxpixel")

                val score = downY - scoreCounter * fontRenderer.FONT_HEIGHT
//                Gui.drawRect(left - 2, score, right, score + fontRenderer.FONT_HEIGHT, 1342177280)
                fontRenderer.drawStringWithShadow(playerName, left.toFloat(), score.toFloat(), -1)
                if (scoreCounter == collection.size) {
                    val s3 = objective.displayName
                    fontRenderer.drawString(
                        s3,
                        left + maxWidth / 2 - fontRenderer.getStringWidth(s3) / 2,
                        score - fontRenderer.FONT_HEIGHT,
                        -1
                    )
                }
            }
        }
    }

    @Subscribe
    fun onShader(e: Shader2DEvent) {
        if(e.type == 0) {
            val scaledRes = ScaledResolution(Minecraft.getMinecraft())
            val right = scaledRes.scaledWidth - 5
            RoundedUtil.drawRound(
                left - 2f,
                downY - maxHeight - 1f,
                right - left + 2f,
                downY + 2f - (downY - maxHeight - 1f),
                2f, true,
                Color(0, 0, 0, 150)
            )
        }
    }
}