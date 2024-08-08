package me.dolphin2410.mazetimer

import io.github.monun.kommand.StringType
import io.github.monun.kommand.getValue
import io.github.monun.kommand.kommand
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title.title
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.util.UUID

class MazeTimerPlugin: JavaPlugin(), Listener {
    private val playerTime = hashMapOf<UUID, Int>()
    private val playerTasks = hashMapOf<UUID, BukkitTask>()
    private val playerNickname = hashMapOf<UUID, String>()
    private val honors = hashMapOf<String, Int>()

    override fun onEnable() {
        kommand {
            register("nickname") {
                then("nickname" to string(StringType.GREEDY_PHRASE)) {
                    executes {
                        val nickname: String by it
                        if (!honors.containsKey(nickname)) {
                            playerNickname[player.uniqueId] = nickname
                            val best = if (honors.size != 0) {
                                honors.values.sorted()[0] * 0.2
                            } else {
                                10000000.0
                            }
                            player.showTitle(title(text("닉네임: $nickname"), text("현재 최고 기록은: ${best}s 입니다")))
                        } else {
                            player.sendMessage(text("이미 존재하는 닉네임입니다!", NamedTextColor.RED))
                        }
                    }
                }
            }
        }

        kommand {
            register("manageractions") {
                then("printhonorsfull") {
                    executes {
                        var index = 1
                        for (entry in honors.toList().sortedBy { it.second }.toMap()) {
                            player.sendMessage("$index : ${entry.key} has finished in ${entry.value}")
                            index += 1
                        }
                    }
                }
                then("printhonors") {
                    executes {
                        var index = 1
                        for (entry in honors.toList().sortedBy { it.second }.toMap()) {
                            if (index == 21) break;
                            player.sendMessage("$index : ${entry.key} has finished in ${entry.value}")
                            index += 1
                        }
                    }
                }
            }
        }

        server.pluginManager.registerEvents(this, this)
    }

    private fun uploadToHonors(nickname: String, elapsedTicks: Int) {
        honors[nickname] = elapsedTicks
    }

    @EventHandler
    fun onPlayerInteract(e: PlayerInteractEvent) {
        if (!playerNickname.containsKey(e.player.uniqueId)) {
            e.player.sendMessage(text("Please use /nickname <nickname>", NamedTextColor.RED))
            return
        }
        if (e.action != Action.PHYSICAL || e.clickedBlock?.type != Material.STONE_PRESSURE_PLATE) return

        val block = e.clickedBlock!!
        val isInit = Checkpoints.initLocations.values.any { block.location.toVector() == it }

        if (playerTasks.containsKey(e.player.uniqueId)) {
            playerTasks[e.player.uniqueId]!!.cancel()
            playerTasks.remove(e.player.uniqueId)
            val nickname = playerNickname.remove(e.player.uniqueId)
            val ticksPassed = playerTime.remove(e.player.uniqueId)

            uploadToHonors(nickname!!, ticksPassed!!)
            val best = if (honors.size != 0) {
                honors.values.sorted()[0] * 0.2
            } else {
                10000000.0
            }

            e.player.showTitle(title(text("경⭐축"), text("당신의 기록: ${String.format("%.2f", ticksPassed * 0.2).toDouble()}s / 최고 기록: ${best}s")))
        }

        if (isInit) {
            playerTasks[e.player.uniqueId] = object: BukkitRunnable() {
                override fun run() {
                    playerTime.computeIfAbsent(e.player.uniqueId) { 0 }
                    playerTime[e.player.uniqueId] = playerTime[e.player.uniqueId]!! + 1
                    val time = playerTime[e.player.uniqueId]!! * 0.2
                    e.player.sendActionBar(text("elapsed time ${String.format("%.2f", time).toDouble()}s", NamedTextColor.AQUA))
                }
            }.runTaskTimer(this, 0, 4)
        }
    }
}