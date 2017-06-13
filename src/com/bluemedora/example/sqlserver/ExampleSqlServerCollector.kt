package com.bluemedora.example.sqlserver

import com.bluemedora.exuno.definition.ExUnoDefinition
import com.bluemedora.exuno.logging.ExUnoLogger
import com.bluemedora.exuno.request.ConnectionInfo
import com.bluemedora.exuno.result.*
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.DecimalFormat
import java.util.regex.Pattern


internal class ExampleSqlServerCollector {
    companion object {
        fun collect(connectionInfo: ConnectionInfo): ICollectionResult {
            val result = ExUnoCollectionResult()

            val systemInfo = time("Adding all system processes from " + connectionInfo.get(ConnectionInfo.HOST)) { addAllProcesses(connectionInfo, result) }
            time("Adding parent PIDs") { addChildProcesses(connectionInfo, result) }
            time("Adding computer resource") { addComputerResource(connectionInfo, result, systemInfo) }

            return result
        }

        private fun addComputerResource(connectionInfo: ConnectionInfo, result: ExUnoCollectionResult, systemResources: Map<String, Double>) {
            val session = sshConnect(connectionInfo)
            session.connect()
            val hostname = performSSHCommand(session, "hostname -A")
            session.disconnect()
            val cpu: Double? = systemResources["cpu"]
            val mem: Double? = systemResources["mem"]

            if (hostname.isNotEmpty()) {
                val hostnameMetric = ExUnoDefinition.toMetric("computer", "hostname", hostname.first())
                val computerResource = ExUnoDefinition.toResource("computer", hostnameMetric)
                if (cpu!! > 90) {
                    computerResource.addEvent(ExUnoDefinition.toEvent("computer", "cpu_limit", IResourceEvent.Severity.SERIOUS, "Critical CPU usage reached."))
                    ExUnoLogger.warn("Critical CPU execution reached at " + DecimalFormat("#.##").format(cpu) + "% usage")
                }
                if (mem!! > 90) {
                    computerResource.addEvent(ExUnoDefinition.toEvent("computer", "mem_limit", IResourceEvent.Severity.SERIOUS, "Critical system memory usage reached."))
                    ExUnoLogger.warn("Critical system memory reached at " + DecimalFormat("#.##").format(mem) + "% usage")
                }
                result.addResource(computerResource)
            }
        }

        private fun addChildProcesses(connectionInfo: ConnectionInfo, result: ExUnoCollectionResult) {
            val session = sshConnect(connectionInfo)
            session.connect()
            val children = performSSHCommand(session, "ps -eo pid,ppid")
            val pat = Pattern.compile("(\\d+)\\s*(\\d+)")

            for (child in children) {
                val m = pat.matcher(child)
                if (!m.find())
                    continue
                val pid = m.group(1).toInt()
                val ppid = m.group(2).toInt()
                val parentR = result.getResourcesWithMetricValue("process", "id", ppid)
                val childR = result.getResourcesWithMetricValue("process", "id", pid)
                if (parentR.isNotEmpty() && childR.isNotEmpty()) {
                    result.addParentChildRelationship(parentR.first(), childR.first())
                }
            }
            session.disconnect()
        }

        fun addAllProcesses(connectionInfo: ConnectionInfo, result: ExUnoCollectionResult): Map<String, Double> {
            val processes = ExampleSqlServerCollector.getAllProcesses(connectionInfo)
            var totalCpu = 0.0
            var totalMem = 0.0

            for (process in processes) {
                val metrics = getMetricsFromTop(process)
                if (metrics.isEmpty())
                    continue
                else {
                    val pid = metrics["pid"]!!.toInt()
                    val cpu = metrics["cpu"]!!.toFloat()
                    totalCpu += cpu
                    val mem = metrics["mem"]!!.toFloat()
                    totalMem += mem

                    val pidMetric = ExUnoDefinition.toMetric("process", "id", pid)
                    val cpuMetric = ExUnoDefinition.toMetric("process", "cpu", cpu)
                    val memMetric = ExUnoDefinition.toMetric("process", "mem", mem)
                    val processResource = ExUnoDefinition.toResource("process", listOf(pidMetric, cpuMetric, memMetric))
                    if (cpu > 90) {
                        processResource.addEvent(ExUnoDefinition.toEvent("computer", "cpu_limit", IResourceEvent.Severity.SERIOUS, "Critical CPU usage reached by process '$pid'."))
                        ExUnoLogger.warn("Critical CPU execution reached at " + DecimalFormat("#.##").format(cpu) + "% usage by process '$pid'.")
                    }
                    if (mem > 90) {
                        processResource.addEvent(ExUnoDefinition.toEvent("computer", "mem_limit", IResourceEvent.Severity.SERIOUS, "Critical system memory usage reached by process '$pid'."))
                        ExUnoLogger.warn("Critical system memory reached at " + DecimalFormat("#.##").format(mem) + "% usage by process '$pid'")
                    }

                    result.addResource(processResource)
                }
            }

            val systemInfo = mapOf("cpu" to totalCpu, "mem" to totalCpu)
            return systemInfo
        }

        fun getMetricsFromTop(line: String): Map<String, Number> {
            val pat = Pattern.compile("(\\d+)\\s*(\\d+\\.\\d+)\\s*(\\d+\\.\\d+)")
            val m = pat.matcher(line)
            if (!m.find())
                return emptyMap()

            val metrics: Map<String, Number> = mapOf("pid" to m.group(1).toInt(), "cpu" to m.group(2).toFloat(), "mem" to m.group(3).toFloat())
            return metrics
        }

        private fun getAllProcesses(connectionInfo: ConnectionInfo): List<String> {

            val session = sshConnect(connectionInfo)
            session.connect()

            val cmd = "top -bn 1 | grep \"^ \" | awk '{printf(\"%-8s %-8s %-8s\\n\", $1, $9, $10); }'"
            val processes = performSSHCommand(session, cmd)

            session.disconnect()

            return processes
        }

        private fun performSSHCommand(session: Session, cmd: String): List<String> {
            val output = mutableListOf<String>()
            try {
                val channel = session.openChannel("exec") as ChannelExec
                channel.inputStream = null
                channel.setErrStream(System.err)
                channel.setCommand(cmd)
                val inputStream = channel.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))
                var line: String?
                channel.connect()

                while (true) {
                    line = reader.readLine()
                    while (line != null) {
                        output.add(line)
                        line = reader.readLine()
                    }
                    if (channel.isClosed) {
                        if (inputStream.available() > 0)
                            continue
                        break
                    }
                }
                ExUnoLogger.debug("'$cmd' output: $output")
                channel.disconnect()
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

