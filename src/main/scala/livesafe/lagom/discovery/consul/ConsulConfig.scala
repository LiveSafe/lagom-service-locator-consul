package livesafe.lagom.discovery.consul

import com.typesafe.config.Config
import com.typesafe.config.ConfigException.BadValue

case class ConsulConfig(
  agentHostname: String,
  agentPort: Int,
  scheme: String,
  routingPolicy: RoutingPolicy,
  registerService: Boolean
)

object ConsulConfig {
  def fromConfig(config: Config): ConsulConfig = {
    ConsulConfig(
      config.getString("lagom.discovery.consul.agent-hostname"),
      config.getInt("lagom.discovery.consul.agent-port"),
      config.getString("lagom.discovery.consul.uri-scheme"),
      RoutingPolicy(config.getString("lagom.discovery.consul.routing-policy")),
      config.getBoolean("lagom.discovery.consul.register-service")
    )
  }
}

object RoutingPolicy {
  def apply(policy: String): RoutingPolicy = policy match {
    case "first" => First
    case "random" => Random
    case "round-robin" => RoundRobin
    case unknown => throw new BadValue("lagom.discovery.consul.routing-policy", s"[$unknown] is not a valid routing algorithm")
  }
}
sealed trait RoutingPolicy
case object First extends RoutingPolicy
case object Random extends RoutingPolicy
case object RoundRobin extends RoutingPolicy
