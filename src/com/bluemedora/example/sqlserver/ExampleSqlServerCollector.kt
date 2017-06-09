package com.bluemedora.example.sqlserver 

import com.bluemedora.exuno.common.ExUnoException
import com.bluemedora.exuno.request.ConnectionInfo
import com.bluemedora.exuno.result.*

internal class ExampleSqlServerCollector
{
    companion object
    {
        fun collect(connectionInfo : ConnectionInfo) : ICollectionResult
        {
            throw ExUnoException("You must implement data collection.")
            //return ExUnoCollectionResult()
        }
    }
}