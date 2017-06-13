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
            var totalCpu = 0.0
            var totalMem = 0.0

            val processes = time("Retrieving all processes from " + connectionInfo.get(ConnectionInfo.HOST)) { ExampleSqlServerCollector.getAllProcesses(connectionInfo) }
            for (process in processes) {
                val pat = Pattern.compile("(\\d+)\\s*(\\d+\\.\\d+)\\s*(\\d+\\.\\d+)")
                val m = pat.matcher(process)
                if (!m.find())
                    continue
                val pid = m.group(1).toInt()
                val cpu = m.group(2).toFloat()
                totalCpu += cpu
                val mem = m.group(3).toFloat()
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
            val session = sshConnect(connectionInfo)
            session.connect()
            val children = performSSHCommand(session, "ps -eo pid,ppid")
            val patt = Pattern.compile("(\\d+)\\s*(\\d+)")

            for(child in children){
                val mm = patt.matcher(child)
                if(!mm.find())
                    continue
                val id = mm.group(1).toInt()
                val ppid = mm.group(2).toInt()
                val parentR = result.getResourcesWithMetricValue("process", "id", ppid)
                val childR = result.getResourcesWithMetricValue("process", "id", id)
                if(parentR.isNotEmpty() && childR.isNotEmpty()){
                    result.addParentChildRelationship(parentR.first(), childR.first())
                }
            }
            session.disconnect()
            //addChildProcesses(connectionInfo, result)

            addComputerResource(connectionInfo, result, totalCpu, totalMem)

            return result
        }

        private fun addComputerResource(connectionInfo: ConnectionInfo, result: ExUnoCollectionResult, cpuUsage: Double, memUsage: Double) {
            val session = sshConnect(connectionInfo)
            session.connect()
            val hostname = performSSHCommand(session, "hostname -A")
            session.disconnect()
            if (hostname.isNotEmpty()) {
                val hostnameMetric = ExUnoDefinition.toMetric("computer", "hostname", hostname.first())
                val computerResource = ExUnoDefinition.toResource("computer", hostnameMetric)
                if (cpuUsage > 1) {
                    computerResource.addEvent(ExUnoDefinition.toEvent("computer", "cpu_limit", IResourceEvent.Severity.SERIOUS, "Critical CPU usage reached."))
                    ExUnoLogger.warn("Critical CPU execution reached at " + DecimalFormat("#.##").format(cpuUsage) + "% usage")
                }
                if (memUsage > 1) {
                    computerResource.addEvent(ExUnoDefinition.toEvent("computer", "mem_limit", IResourceEvent.Severity.SERIOUS, "Critical system memory usage reached."))
                    ExUnoLogger.warn("Critical system memory reached at " + DecimalFormat("#.##").format(memUsage) + "% usage")
                }
                result.addResource(computerResource)
            }
        }

        private fun addChildProcesses(connectionInfo: ConnectionInfo, result: ExUnoCollectionResult) {
            val session = sshConnect(connectionInfo)
            session.connect()

            for (resource in result.resources) {
                val pid = resource.getMetric("id").intValue
                val children = time("Getting all children of process $pid") { getChildProcesses(session, pid) }
                for (child in children) {
                    val childResource = result.getResourcesWithMetricValue("process", "id", child)
                    if (childResource.isEmpty())
                        ExUnoLogger.info("Child process '$child' could not be found in result.")
                    else
                        result.addParentChildRelationship(resource, childResource.first())
                }
            }

            session.disconnect()
        }

        private fun getChildProcesses(session: Session, pid: Int): List<String> {

            val cmd = "pgrep -P $pid"
            val children = performSSHCommand(session, cmd)

            return children
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

