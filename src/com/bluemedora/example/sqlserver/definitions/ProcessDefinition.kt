package com.bluemedora.example.sqlserver.definitions

import com.bluemedora.exuno.common.ExUnoCategory
import com.bluemedora.exuno.common.ExUnoUnit
import com.bluemedora.exuno.definition.EventDefinition
import com.bluemedora.exuno.definition.MetricDefinition
import com.bluemedora.exuno.definition.ResourceDefinition

object ProcessDefinition {
    val definition: ResourceDefinition = ResourceDefinition("process", "Process")
            .withMetricDefinitions(
                    MetricDefinition("id", "ID")
                            .withUnits(ExUnoUnit.UNITLESS)
                            .asInteger()
                            .asKey()
                            .withDescription("Process ID.")
                            .withCategory(ExUnoCategory.DETAILS),
                    MetricDefinition("cpu", "CPU Usage")
                            .withUnits(ExUnoUnit.PERCENT)
                            .asFloat()
                            .withDescription("CPU Usage.")
                            .withCategory(ExUnoCategory.CPU),
                    MetricDefinition("mem", "Memory Usage")
                            .withUnits(ExUnoUnit.PERCENT)
                            .asFloat()
                            .withDescription("Memory Usage.")
                            .withCategory(ExUnoCategory.MEMORY)
            )
    val cpuEvent: EventDefinition = EventDefinition("cpu_limit", "CPU has reached a critical state for a process").asCritical()
    val memEvent: EventDefinition = EventDefinition("mem_limit", "Memory has reached a critical state for a process").asCritical()
}
