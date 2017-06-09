package com.bluemedora.example.sqlserver

import com.bluemedora.exuno.common.ExUnoException
import com.bluemedora.exuno.definition.ExUnoDefinition
import com.bluemedora.exuno.definition.MetricDefinition
import com.bluemedora.exuno.logging.ExUnoLogger
import com.bluemedora.exuno.request.ConnectionInfo
import com.bluemedora.exuno.result.*
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*
import java.util.regex.Pattern
import com.bluemedora.example.sqlserver.time


internal class ExampleSqlServerCollector {
    companion object {
        fun collect(connectionInfo: ConnectionInfo): ICollectionResult {
            val result = ExUnoCollectionResult()

            val pids = time("testing time") {ExampleSqlServerCollector.getAllPids(connectionInfo)}
            for (process in pids) {
                val pid = Pattern.compile("(\\d+) (.+)")
                val mid = pid.matcher(process)
                if (!mid.find())
                    continue
                println(mid.group(1))
                println(mid.group(2))
                val pidMetric = ExUnoDefinition.toMetric("process", "id", mid.group(1).toInt())
                val processResource = ExUnoDefinition.toResource("process", listOf(pidMetric))
                result.addResource(processResource)
            }

            return result
        }

        fun getAllPids(connectionInfo: ConnectionInfo): List<String> {
            val output = mutableListOf<String>()
            try {
                val session = sshConnect(connectionInfo)
                session.connect()

                val channel = session.openChannel("exec") as ChannelExec
                val cmd = "ps -eo pid,command"
                session.openChannel("exec")
                channel.setCommand(cmd)
                channel.inputStream = null
                channel.setErrStream(System.err)

                val inputStream = channel.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))
                var line: String?
                channel.connect()

                while (true) {
                    line = reader.readLine()
                    var j = 0
                    while (line != null) {
                        output.add(line)
                        line = reader.readLine()
                        j += 1
                    }

                    if (channel.isClosed) {
                        if (inputStream.available() > 0)
                            continue
                        println("exit-status: " + channel.exitStatus)
                        break
                    }

                    /*try {
                        Thread.sleep(1000)
                    } catch (e: Exception) {
                        ExUnoLogger.error("Exception with message: " + e.message)
                    }*/
                }
                channel.disconnect()
                session.disconnect()
            } catch (ee: Exception) {
                ExUnoLogger.error("Exception with message: " + ee.message)
            }

            return output
        }

        private fun sshConnect(connectionInfo: ConnectionInfo): Session {
            val config = java.util.Properties()
            val host = connectionInfo.get("host")
            val username = connectionInfo.get("username")
            val password = connectionInfo.get("password")
            val jsch: JSch = JSch()
            val session: Session = jsch.getSession(username, host, 22)

            session.setPassword(password)
            config.put("StrictHostKeyChecking", "no")
            session.setConfig(config)
            return session
        }
    }


}

