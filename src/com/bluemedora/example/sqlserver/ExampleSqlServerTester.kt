package com.bluemedora.example.sqlserver

import com.bluemedora.exuno.request.ConnectionInfo
import com.bluemedora.exuno.result.*

internal class ExampleSqlServerTester
{
    companion object
    {
        fun testConnection(connectionInfo : ConnectionInfo) : ITestConnectionResult
        {
            return ExUnoTestConnectionResult(ExUnoTestResult("Template Test", "Please implement your own test", false))
        }
    }
}