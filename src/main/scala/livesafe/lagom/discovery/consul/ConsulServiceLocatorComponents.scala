package livesafe.lagom.discovery.consul

import com.ecwid.consul.v1.ConsulClient
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.client.{ CircuitBreakerComponents, LagomServiceClientComponents }

/**
 * This creates a ConsulServiceLocator and binds it to the 'serviceLocator' member of 'LagomServiceClientComponents'
 */
trait ConsulServiceLocatorComponents
  // extends CircuitBreakerComponents
{ this: LagomServiceClientComponents with CircuitBreakerComponents =>
  //def actorSystem: ActorSystem
  // def configuration: Configuration
  //def executionContext: ExecutionContext
  //def circuitBreakerMetricsProvider: CircuitBreakerMetricsProvider

  lazy val consulConfig: ConsulConfig = ConsulConfig.fromConfig(config)
  lazy val consulClient: ConsulClient = new ConsulClient(consulConfig.agentHostname, consulConfig.agentPort)

  lazy override val serviceLocator: ServiceLocator = new ConsulServiceLocator(consulClient, consulConfig, circuitBreakers)(executionContext)
}

