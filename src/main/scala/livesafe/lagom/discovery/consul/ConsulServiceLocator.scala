package livesafe.lagom.discovery.consul

import java.net.{ InetAddress, URI }
import java.util.concurrent.ThreadLocalRandom

import scala.collection.JavaConverters._
import scala.collection.concurrent.{ Map, TrieMap }
import scala.concurrent.{ ExecutionContext, Future }

import com.ecwid.consul.v1.catalog.model.CatalogService
import com.ecwid.consul.v1.{ ConsulClient, QueryParams }
import com.lightbend.lagom.internal.client.CircuitBreakers
import com.lightbend.lagom.scaladsl.api.Descriptor
import com.lightbend.lagom.scaladsl.client.CircuitBreakingServiceLocator

class ConsulServiceLocator(
  client: ConsulClient,
  config: ConsulConfig,
  circuitBreakers: CircuitBreakers
)(implicit ec: ExecutionContext)
  extends CircuitBreakingServiceLocator(circuitBreakers) {

  private val roundRobinIndexFor: Map[String, Int] = TrieMap.empty[String, Int]

  override def locate(name: String, serviceCall: Descriptor.Call[_, _]): Future[Option[URI]] = locateAsScala(name)

  private def locateAsScala(name: String): Future[Option[URI]] = Future {
    val instances = client.getCatalogService(name, QueryParams.DEFAULT).getValue.asScala.toList
    val instanceURIs = instances.map(serviceToUri).sorted
    instanceURIs.size match {
      case 0 => None
      case 1 => instanceURIs.headOption
      case _ =>
        config.routingPolicy match {
          case First => Some(pickFirstInstance(instanceURIs))
          case Random => Some(pickRandomInstance(instanceURIs))
          case RoundRobin => Some(pickRoundRobinInstance(name, instanceURIs))
        }
    }
  }

  private implicit object DefaultOrdering extends Ordering[URI] {
    override def compare(x: URI, y: URI): Int = x.compareTo(y)
  }

  private[consul] def pickFirstInstance(services: List[URI]): URI = {
    if (services.isEmpty) throw new IllegalStateException("List of services should not be empty")
    services.head
  }

  private[consul] def pickRandomInstance(services: List[URI]): URI = {
    if (services.isEmpty) throw new IllegalStateException("List of services should not be empty")
    services.apply(ThreadLocalRandom.current.nextInt(services.size - 1))
  }

  private[consul] def pickRoundRobinInstance(name: String, services: List[URI]): URI = {
    if (services.isEmpty) throw new IllegalStateException("List of services should not be empty")
    roundRobinIndexFor.putIfAbsent(name, 0)
    val currentIndex = roundRobinIndexFor(name)
    val nextIndex =
      if (services.size > currentIndex + 1) currentIndex + 1
      else 0
    roundRobinIndexFor.replace(name, nextIndex)
    services.apply(currentIndex)
  }

  private def serviceToUri(service: CatalogService): URI = {
    val address = service.getServiceAddress
      val serviceAddress =
        if (address.trim.isEmpty || address == "localhost") InetAddress.getLoopbackAddress.getHostAddress
        else address
      new URI(s"${config.scheme}://$serviceAddress:${service.getServicePort}")
  }

  private def toURIs(services: List[CatalogService]): List[URI] = services.map(serviceToUri)

}
