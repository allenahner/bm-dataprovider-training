package com.bluemedora.example.sqlserver

import com.bluemedora.example.sqlserver.definitions.ComputerDefinition
import com.bluemedora.example.sqlserver.definitions.ProcessDefinition
import com.bluemedora.exuno.ISelfDescribingDataCollector
import com.bluemedora.exuno.request.ConnectionInfo
import com.bluemedora.exuno.result.*
import com.bluemedora.exuno.definition.*


class ExampleSqlServerDataCollector : ISelfDescribingDataCollector {
    override fun getCollectorType(): String {
        return ExampleSqlServerDataCollectorProperties.DATA_COLLECTOR_TYPE
    }

    override fun getCollectorDescription(): String {
        return ExampleSqlServerDataCollectorProperties.DATA_COLLECTOR_DESCRIPTION
    }

    override fun getCollectorLabel(): String {
        return ExampleSqlServerDataCollectorProperties.DATA_COLLECTOR_LABEL
    }

    override fun getCollectorVersion(): String {
        return ExampleSqlServerDataCollectorProperties.DATA_COLLECTOR_VERSION
    }

    override fun describeTestConnectionResult(): ITestConnectionResultDefinition {
        return ExampleSqlServerTester.getTestConnectionResultDefinition();
    }

    override fun describeCollectionResult(): ICollectionResultDefinition {
        return COLLECTION_RESULT_DEFINITION
    }

    /**
     * Retrieves a definition of parameters accepted by this data collector.
     *
     * @return A ConnectionInfoDefinition. Use this to validate parameters defined in the connection info.
     */
    override fun describeConnectionInfo(): IConnectionInfoDefinition {
        return ExampleSqlServerConnectionInfoDefinition.getConnectionInfoDefinition()
    }

    /**
     * Validates parameters accepted by this data collector.
     *
     * @param connectionInfo The parameters needed to connect to the target
     * @return A ITestConnectionResult, which explains which parameters are valid/invalid
     */
    override fun validateConnectionInfo(connectionInfo: ConnectionInfo): ITestConnectionResult {
        return describeConnectionInfo().validate(connectionInfo)
    }

    /**
     * Test connectivity to the target.
     *
     * @param connectionInfo The parameters needed to connect to the target
     * @return A TestConnectionResult, which details the results of the test
     */
    override fun testConnection(connectionInfo: ConnectionInfo): ITestConnectionResult {
        return ExampleSqlServerTester.testConnection(connectionInfo)
    }

    /**
     * Method that collects data. By default, all available is collected.
     *
     * @param connectionInfo The parameters needed to connect to the target
     * @return A CollectionResult, which contains all information collected
     */
    override fun collect(connectionInfo: ConnectionInfo): ICollectionResult {
        return ExampleSqlServerCollector.collect(connectionInfo)
    }

    companion object {
        val COLLECTION_RESULT_DEFINITION: CollectionResultDefinition = CollectionResultDefinition()
                .withResourceDefinition(ProcessDefinition.definition
                                                .withEventDefinition(ProcessDefinition.cpuEvent)
                                                .withEventDefinition(ProcessDefinition.memEvent)
                )
                .withResourceDefinition(ComputerDefinition.definition
                                                .withEventDefinition(ComputerDefinition.cpuEvent)
                                                .withEventDefinition(ComputerDefinition.memEvent)
                )
    }
}
