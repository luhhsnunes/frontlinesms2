package frontlinesms2

class TrashService {
    	def emptyTrash() {
		Fmessage.findAllByIsDeleted(true)*.delete()
		MessageOwner.findAllByDeleted(true)*.delete()
		Trash.findAll()*.delete()
    	}
    
	def sendToTrash(object) {
		if (object instanceof frontlinesms2.Fmessage) {
			object.isDeleted = true
			new Trash(displayName:object.displayName,
					displayText:object.text,
					objectClass:object.class.name,
					objectId:object.id).save()
			object.save(failOnError:true, flush:true)
		} else if (object instanceof frontlinesms2.MessageOwner) {
			object.deleted = true
			object.messages.each {
				it.isDeleted = true
				it.save(failOnError: true, flush: true)
			}
			def detail = object.messages?.size() + " message(s)"
			new Trash(displayName:object.name,
					displayText:detail,
					objectClass:object.class.name,
					objectId:object.id).save()
			object.save(failOnError:true, flush:true)
		}
	}

	def restore(object) {
		Trash.findByObject(object)?.delete()
		object.restoreFromTrash()
		object.save()
		return true
	}
}

