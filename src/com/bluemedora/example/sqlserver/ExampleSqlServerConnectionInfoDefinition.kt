package com.bluemedora.example.sqlserver

import com.bluemedora.exuno.definition.*
import com.bluemedora.exuno.request.ConnectionInfo

internal class ExampleSqlServerConnectionInfoDefinition {
    companion object {
        fun getConnectionInfoDefinition(): IConnectionInfoDefinition {
            return ConnectionInfoDefinition()
                    .with(ParameterDefinition(ConnectionInfo.USERNAME))
                    .with(ParameterDefinition(ConnectionInfo.PASSWORD))
                    .with(HostnameParameterDefinition(ConnectionInfo.HOST).acceptFqdn().acceptShortName().acceptIpv4().acceptIpv6())
        }
    }
}