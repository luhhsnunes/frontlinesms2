package frontlinesms2

import grails.util.Environment

import org.apache.camel.builder.RouteBuilder
import org.apache.camel.model.RouteDefinition

// Please don't instantiate this class.  We would make it abstract if it didn't make testing
// difficult, and stop us calling GORM queries across all subclasses.
class Fconnection {
	def fconnectionService
	static transients = ['status', 'routeDefinitions']
	static String getShortName() { 'base' }
	
	static def implementations = [SmslibFconnection,
			ClickatellFconnection,
			IntelliSmsFconnection]
	static getNonnullableConfigFields = { clazz ->
		def fields = clazz.configFields
		if(fields instanceof Map) return fields.getAllValues()?.findAll { field -> !clazz.constraints[field].blank }
		else return fields.findAll { field -> if(!(clazz.metaClass.hasProperty(null, field).type in [Boolean, boolean])){ !clazz.constraints[field].nullable} }
	}

	static mapping = {
		tablePerHierarchy false
	}
	
	String name
	
	def getStatus() {
		fconnectionService.getConnectionStatus(this)
	}
	
	List<RouteDefinition> getRouteDefinitions() {
		if(Environment.current != Environment.TEST) {
			throw new IllegalStateException("Do not know how to create routes for Fconnection of class: ${this.class}")
		}
		return new RouteBuilder() {
			@Override void configure() {}
			List getRouteDefinitions() {
				return [
					from('seda:nowhere')
							.to('bad:fconnection?subclassed=false')
							.routeId("out-${Fconnection.this.id}"),
					from('bad:fconnection?subclassed=false')
							.onException(NotConnectedException)
									.handled(true)
									.beanRef('fconnectionService', "handleDisconnection")
									.end()
							.to('stream:out').routeId("in-${Fconnection.this.id}")]
			}
		}.routeDefinitions
	}
}
