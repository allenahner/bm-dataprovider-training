package com.bluemedora.example.sqlserver;

import com.bluemedora.exuno.common.ExUnoDate
import com.bluemedora.exuno.definition.*
import com.bluemedora.exuno.request.ConnectionInfo

internal class ExampleSqlServerConnectionInfoDefinition
{
    companion object
    {
        fun getConnectionInfoDefinition(): IConnectionInfoDefinition
        {
            return ConnectionInfoDefinition()
                .with(ParameterDefinition(ConnectionInfo.USERNAME))
                .with(ParameterDefinition(ConnectionInfo.PASSWORD))
                .with(HostnameParameterDefinition(ConnectionInfo.HOST).acceptFqdn().acceptShortName().acceptIpv4().acceptIpv6())
                .with(SslParameterDefinition(ConnectionInfo.SSL_CONFIG).asOptionalWithDefault(SslParameterDefinition.NO_VERIFY))
                .with(PortParameterDefinition(ConnectionInfo.PORT).asOptionalWithDefault(8443))
                .with(BooleanParameterDefinition(ConnectionInfo.COLLECT_EVENTS).asOptionalWithDefault(true).withDescription("Whether or not to collect events."))
                .with(BoundedIntegerParameterDefinition(ConnectionInfo.MAX_EVENTS).withMin(1).withMax(100000).asOptionalWithDefault(10000).withDescription("The maximum number of events to collect."))
                .with(TimestampParameterDefinition(ConnectionInfo.EVENT_CUTOFF_TIME).asOptionalWithDefault(ExUnoDate.Time.ONE_DAY_AGO).withDescription("Events before this time will not be collected."))
        }
    }
}