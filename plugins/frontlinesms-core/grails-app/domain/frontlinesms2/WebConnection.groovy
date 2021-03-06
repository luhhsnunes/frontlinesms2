package frontlinesms2

import org.apache.camel.*
import org.apache.camel.Exchange
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.model.RouteDefinition
import frontlinesms2.camel.exception.*

class WebConnection extends Activity {
	def camelContext
	def webConnectionService
	enum HttpMethod { POST, GET }
	static String getShortName() { 'webConnection' }

	// Camel route redelivery config
	static final def retryAttempts = 3 // how many times to retry before giving up
	static final def initialRetryDelay = 10000 // delay before first retry, in milliseconds
	static final def delayMultiplier = 3 // multiplier applied to delay after each failed attempt

	// Substitution variables
	static subFields = ['message_body' : { msg ->
			def keyword = msg.messageOwner?.keyword?.value
			def text = msg.text
			if (keyword?.size() && text.toUpperCase().startsWith(keyword.toUpperCase())) {
				text = text.substring(keyword.size()).trim()
			}
			text
		},
		'message_body_with_keyword' : { msg -> msg.text },
		'message_src_number' : { msg -> msg.src },
		'message_src_name' : { msg -> Contact.findByMobile(msg.src)?.name ?: msg.src },
		'message_timestamp' : { msg -> msg.dateCreated }]
	
	/// Variables
	String url
	HttpMethod httpMethod
	static hasMany = [requestParameters:RequestParameter]
	static hasOne = [keyword: Keyword]
	
	static constraints = {
		name(blank:false, maxSize:255, validator: { val, obj ->
			if(obj?.deleted || obj?.archived) return true
			def identical = WebConnection.findAllByNameIlike(val)
			if(!identical) return true
			else if (identical.any { it.id != obj.id && !it?.archived && !it?.deleted }) return false
			else return true
			})
	}
	static mapping = {
		requestParameters cascade: "all-delete-orphan"
	}

	def processKeyword(Fmessage message, Boolean exactMatch) {
		this.addToMessages(message)
		this.save(failOnError:true)
		webConnectionService.send(message)
	}

	List<RouteDefinition> getRouteDefinitions() {
		return new RouteBuilder() {
			@Override void configure() {}
			List getRouteDefinitions() {
				return [from("seda:activity-webconnection-${WebConnection.this.id}")
						.beanRef('webConnectionService', 'preProcess')
						.setHeader(Exchange.HTTP_PATH,
								simple('${header.url}'))
						.onException(Exception)
									.redeliveryDelay(initialRetryDelay)
									.backOffMultiplier(delayMultiplier)
									.maximumRedeliveries(retryAttempts)
									.retryAttemptedLogLevel(LoggingLevel.WARN)
									.handled(true)
									.beanRef('webConnectionService', 'handleException')
									.end()
						.to(url)
						.beanRef('webConnectionService', 'postProcess')
						.routeId("activity-webconnection-${WebConnection.this.id}")]
			}
		}.routeDefinitions
	}

	def activate() {
		println "*** ACTIVATING ACTIVITY ***"
		try {
			def routes = this.routeDefinitions
			camelContext.addRouteDefinitions(routes)
			println "################# Activating WebConnection :: ${this}"
			LogEntry.log("Created WebConnection routes: ${routes*.id}")
		} catch(FailedToCreateProducerException ex) {
			logFail(this, ex.cause)
		} catch(Exception ex) {
			logFail(this, ex)
			camelContext.stopRoute("activity-webconnection-${this.id}")
			camelContext.removeRoute("activity-webconnection-${this.id}")
		}
	}

	def deactivate() {
		println "################ Deactivating WebConnection :: ${this}"
		camelContext.stopRoute("activity-webconnection-${this.id}")
		camelContext.removeRoute("activity-webconnection-${this.id}")
	}
}
	
