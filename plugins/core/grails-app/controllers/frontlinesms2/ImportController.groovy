package frontlinesms2
import java.io.File;
import java.io.InputStream;
import java.io.Reader;

class ImportController {
	def importContacts = {
		def savedCount = 0
		def failedCount = 0
		def uploadedCSVFile = request.getFile('importCsvFile')
		
		if(uploadedCSVFile) {
			def headers
			def standardFields = ['Name':'name', 'Mobile Number':'primaryMobile',
					'E-mail Address':'email', 'Notes':'notes']
			uploadedCSVFile.inputStream.toCsvReader([escapeChar:'�']).eachLine { tokens ->
				if(!headers) headers = tokens
				else try {
					Contact c = new Contact()
					def groups
					headers.eachWithIndex { key, i ->
						def value = tokens[i]
						if(key in standardFields) {
							c."${standardFields[key]}" = value
						} else if(key == 'Group(s)') {
							def groupNames = getGroupNames(value)
							println "Group names: $groupNames"
							groups = getGroups(groupNames)
						} else {
							new CustomField(name:key, value:value, contact:c)
						}
					}
					c.save(failOnError:true)
					if(groups) groups.each { c.addToGroup(it) }
					++savedCount
				} catch(Exception ex) {
					ex.printStackTrace() // TODO replace this with logging
					++failedCount
				}
			}
			flash.message = "$savedCount contacts were imported; $failedCount failed" 
			redirect controller: "settings", action: 'general'
		} else throw new RuntimeException("File upload has failed for some reason.")
	}
	
	def getGroupNames(csvValue) {
		println "getGroupNames() : csvValue=$csvValue"
		Set csvGroups = []
		csvValue.split("\\\\").each { gName ->
			def longName
			gName.split("/").each { shortName ->
				csvGroups << shortName
				longName = longName? "$longName-$shortName": shortName
				csvGroups << longName
			}
		}
		println "getGroupNames() : ${csvGroups - ''}"
		return csvGroups - ''
	}
	
	def getGroups(groupNames) {
		println "ImportController.getGroups() : $groupNames"
		groupNames.collect { name ->
			Group.findByName(name)?: new Group(name:name).save(failOnError:true)
		}
	}
}