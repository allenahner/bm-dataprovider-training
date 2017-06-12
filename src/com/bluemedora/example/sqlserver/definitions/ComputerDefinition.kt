package com.bluemedora.example.sqlserver.definitions

import com.bluemedora.exuno.common.ExUnoUnit
import com.bluemedora.exuno.definition.EventDefinition
import com.bluemedora.exuno.definition.MetricDefinition
import com.bluemedora.exuno.definition.ResourceDefinition

object ComputerDefinition {
    val definition: ResourceDefinition = ResourceDefinition("computer", "Computer")
            .withMetricDefinitions(
                    MetricDefinition("hostname", "Hostname")
                            .withUnits(ExUnoUnit.UNITLESS)
                            .asString()
                            .asKey()
                            .withDescription("Hostname")
            )
    val cpuEvent: EventDefinition = EventDefinition("cpu_limit", "CPU has reached a critical state")
    val memEvent: EventDefinition = EventDefinition("mem_limit", "Memory has reached a critical state")
}
