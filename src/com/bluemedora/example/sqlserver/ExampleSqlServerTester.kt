package com.bluemedora.example.sqlserver

import com.bluemedora.exuno.definition.ITestConnectionResultDefinition
import com.bluemedora.exuno.definition.TestConnectionResultDefinition
import com.bluemedora.exuno.definition.TestResultDefinition
import com.bluemedora.exuno.logging.ExUnoLogger
import com.bluemedora.exuno.request.ConnectionInfo
import com.bluemedora.exuno.result.ExUnoTestConnectionResult
import com.bluemedora.exuno.result.ExUnoTestResult
import com.bluemedora.exuno.result.ITestConnectionResult
import com.jcraft.jsch.*

internal class ExampleSqlServerTester {
    companion object {
        fun testConnection(connectionInfo: ConnectionInfo): ITestConnectionResult {
            // Create an empty ExUnoTestConnectionResult
            val connectionResult = ExUnoTestConnectionResult()

            try {
                // Retrieve the hostname, username, and password properties from connectionInfo
                val host = connectionInfo.get("host")
                val username = connectionInfo.get("username")
                val password = connectionInfo.get("password")
                val canConnect: Boolean

                // Create a SSH Connection
                canConnect = this.connectToHost(host, username, password)

                // Create an ExUnoTestResult base on whether the connection worked
                val testResult = ExUnoTestResult("connect_to_host", "Connect to host", canConnect)

                // Add the ExUnoTestResult as a crtical result to the ExUnoTestConnectionResult
                connectionResult.addCriticalResult(testResult)
            } catch (e: Exception) {
                ExUnoLogger.error("Connection failed with message: " + e.message)
            }

            //Return the ExUnoTestResult
            return connectionResult
        }

        fun getTestConnectionResultDefinition(): ITestConnectionResultDefinition {
            return TestConnectionResultDefinition()
                    .with(TestResultDefinition("connect_to_host", "Connect to host"))
        }

        fun connectToHost(host: String, username: String, password: String): Boolean {
            try {
                val config = java.util.Properties()
                val jsch = JSch()
                val session = jsch.getSession(username, host, 22)
                session.setPassword(password)
                config.put("StrictHostKeyChecking", "no")
                session.setConfig(config)
                session.connect()
                session.disconnect()
                return true
            } catch (e: Exception) {
                return false
            }
        }

    }

}