package com.bluemedora.example.sqlserver.definitions

import com.bluemedora.exuno.common.ExUnoUnit
import com.bluemedora.exuno.definition.MetricDefinition
import com.bluemedora.exuno.definition.ResourceDefinition

object ProcessDefinition {
    val definition: ResourceDefinition = ResourceDefinition("process", "Process")
            .withMetricDefinitions(MetricDefinition("id", "ID")
                                           .withUnits(ExUnoUnit.UNITLESS)
                                           .asInteger()
                                           .asKey()
                                           .withDescription("Process ID.")
            )
}
