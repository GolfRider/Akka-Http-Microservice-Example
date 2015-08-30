import akka.actor.ActorSystem
import akka.event.{LoggingAdapter, Logging}
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import java.io.IOException
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.math._
import spray.json.DefaultJsonProtocol
import akka.http.scaladsl.server.RequestContext

case class SampleRequest(name:String,model:String)

trait Protocol extends DefaultJsonProtocol {
   implicit val sampleRequestFormat=jsonFormat2(SampleRequest)
}


object AkkaHttpMicroServiceExample extends App with Protocol{

	implicit val system=ActorSystem("akka-http-micro-service-example")
	sys.addShutdownHook({ system.shutdown() })

	implicit val executor= system.dispatcher
	implicit val materializer = ActorMaterializer()

	val config = ConfigFactory.load()
	val logger = Logging(system,getClass)

	def getCar(request:HttpRequest,id:Integer):SampleRequest={
	  SampleRequest("VW","Golf Request for :"+request.getUri)
	}

	def postCar(req:SampleRequest,http:HttpRequest):SampleRequest={
	  SampleRequest("VW Golf ","is shipping to url :"+http.getUri)
	}


	val routes=    path("car" / IntNumber) { id =>
	  get { ctx =>
			ctx.complete{ 
						getCar(ctx.request,id)  
				}
	  
		}~
	   post {  
			  entity(as[SampleRequest]) {
						 req =>
						 ctx:RequestContext => 
						 ctx.complete {
								postCar(req,ctx.request)
						 }
			  }
		 }   

	  }

	Http().bindAndHandle(routes,"0.0.0.0",9000)

}
