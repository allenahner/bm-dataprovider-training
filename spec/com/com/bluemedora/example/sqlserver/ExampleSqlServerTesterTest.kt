package com.bluemedora.example.sqlserver

import com.bluemedora.example.sqlserver.definitions.*
import com.bluemedora.exuno.definition.CollectionResultDefinition
import com.bluemedora.exuno.definition.ExUnoDefinition
import com.bluemedora.exuno.request.ConnectionInfo
import com.bluemedora.exuno.result.ExUnoCollectionResult
import com.bluemedora.exuno.result.ExUnoResource
import io.kotlintest.KTestJUnitRunner
import io.kotlintest.forAll
import io.kotlintest.matchers.*
import io.kotlintest.specs.ShouldSpec
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)

class ExampleSqlServerTesterTest : ShouldSpec() {
    init {
        val connectionInfo = ConnectionInfo()
        connectionInfo.add(ConnectionInfo.USERNAME, "root")
        connectionInfo.add(ConnectionInfo.PASSWORD, "password")
        connectionInfo.add(ConnectionInfo.HOST, "rh6kb")

        "When given results, other than column names, from the `top` command, the output"{
            should("be a Map with pid, cpu, and mem indices") {
                val topOut = "1     \t8.0\t\t\t\t100.0"
                val metrics = ExampleSqlServerCollector.getMetricsFromTop(topOut)
                metrics should haveKey("pid")
                metrics should haveKey("cpu")
                metrics should haveKey("mem")
            }
        }

        "Column names from `top` command"{
            should("should return empty map") {
                val topColNames = "PID 1CPU11.101%\t\t      MEM"
                ExampleSqlServerCollector.getMetricsFromTop(topColNames).isEmpty() shouldBe true
            }
        }

        "Adding all processes "{
            should("should result in a list of process resources with an ID metric") {
                val result = ExUnoCollectionResult()
                ExUnoDefinition.set(CollectionResultDefinition()
                                            .withResourceDefinition(ComputerDefinition.definition)
                                            .withResourceDefinition(ProcessDefinition.definition)
                )

                ExampleSqlServerCollector.addAllProcesses(connectionInfo, result)
                forAll(result.getResourcesWithMetricTypes("process", "id")) { process ->
                    process should beOfType<ExUnoResource>()
                }
            }
        }
    }
}