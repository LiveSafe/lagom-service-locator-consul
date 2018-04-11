package livesafe.lagom.discovery.consul

import java.net.InetAddress
import java.util.UUID

import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }

import com.ecwid.consul.v1.ConsulClient
import com.ecwid.consul.v1.agent.model.NewService
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.client.{ CircuitBreakerComponents, LagomServiceClientComponents }
import org.slf4j.{ Logger, LoggerFactory }

/**
 * This creates a ConsulServiceLocator and binds it to the 'serviceLocator' member of 'LagomServiceClientComponents'
 * Additionally, it registers itself as a service with consul, and adds a shutdown hook to de-register
 */
trait ConsulServiceLocatorComponents { this: LagomServiceClientComponents with CircuitBreakerComponents =>

  private final val log: Logger = LoggerFactory.getLogger(getClass)

  lazy val consulServiceId: String = UUID.randomUUID().toString
  lazy val consulConfig: ConsulConfig = ConsulConfig.fromConfig(config)
  lazy val consulClient: ConsulClient = new ConsulClient(consulConfig.agentHostname, consulConfig.agentPort)

  lazy override val serviceLocator: ServiceLocator = new ConsulServiceLocator(consulClient, consulConfig, circuitBreakersPanel)(executionContext)

  private def registerService(): Unit = {
    val hostname = InetAddress.getLocalHost().getHostAddress()
    val port = config.getInt("play.http.port")
    val healthEndpoint = "healthcheck"
    val healthCheckUrl = s"http://${hostname}:${port}/${healthEndpoint}"

    val service = {
      val service = new NewService()
      service.setId(consulServiceId)
      service.setName(serviceInfo.serviceName)
      service.setPort(port)
      service.setAddress(hostname)
      val serviceCheck = {
        val check = new NewService.Check();
        check.setHttp(healthCheckUrl);
        check.setInterval("5s");
        check.setTimeout("1s");
        check
      }
      service.setCheck(serviceCheck)
      service
    }
    Try(consulClient.agentServiceRegister(service)) match {
      case Success(response) =>
        log.info(s"Registered service ${serviceInfo.serviceName} with ${hostname}:${port} at ${consulConfig.agentHostname}:${consulConfig.agentPort} with response \n${response.toString}")
      case Failure(ex) =>
        log.error(s"Failed to register service ${serviceInfo.serviceName} with ${hostname}:${port} at ${consulConfig.agentHostname}:${consulConfig.agentPort}", ex)

    }
  }

  if (consulConfig.registerService) {
    registerService()
  }

  applicationLifecycle.addStopHook(() =>
     Future { consulClient.agentServiceDeregister(consulServiceId) }(executionContext)
   )

}

