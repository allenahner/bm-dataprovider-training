package com.bluemedora.example.sqlserver.definitions

import com.bluemedora.exuno.common.ExUnoCategory
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
                            .asProperty()
                            .withDescription("Hostname")
                            .withCategory(ExUnoCategory.DETAILS)
            )
    val cpuEvent: EventDefinition = EventDefinition("computer_cpu_limit", "CPU has reached a critical state").asCritical()
    val memEvent: EventDefinition = EventDefinition("computer_mem_limit", "Memory has reached a critical state").asCritical()
}
