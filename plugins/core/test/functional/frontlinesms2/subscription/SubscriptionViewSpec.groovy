package frontlinesms2.subscription

import frontlinesms2.*
import frontlinesms2.contact.*
import frontlinesms2.message.*
import frontlinesms2.popup.*

class SubscriptionViewSpec extends SubscriptionBaseSpec {
	def "subscription page should show the details of the subscription in the header"(){
		setup:
			def subscription  = Subscription.findBy.....
		when:
			to PageSubscriptionView, subscription
		then:
			waitFor { page to Load}
			subHead.name  == subscription.name
			subHead.joinautoreply ==  subscription.joinautoreply
			subHead.leaveautoreply == subscription.leaveautoreply
			subHead.group == subscription.group
			subHead.groupcount ==  subscription.groupcount
			subHead.toggleEnable == subscription.toggleEnable
	}

	def "clicking the group link shoud redirect to the group page"(){
		when:
			to PageSubscriptionView, Subscription.findBy....
		then:
			waitFor { subHead.displayed }
		when:
			subHead.groupLink.click()
		then:
			waitFor { at PageGroup }
			//subscription.group.name == header.groupname
	}

	def "clicking the archive button archives the subscription and redirects to inbox "(){
		when:
			to PageSubscriptionView, Subscription.findBy....
		then:
			waitFor { subHead.displayed }
		when:
			header.archive.click()
		then:
			waitFor { at PageMessageInbox }
			notifications.flashmessage.contains("subscription has been archived")
	}

	def "clicking the edit option opens the Subscription Dialog for editing"(){
		when:
			to PageSubscriptionView, Subscription.findBy....
		then:
			waitFor { subHead.displayed }
		when:
			header.moreAction("edit").jquery.click()
		then:
			waitFor { at SubscriptionDialog }
			waitFor { something in the SubscriptionDialog is displayed }
	}

	def "clicking the group link shoud redirect to the group page"(){}

	// FIXME this is a test skeleton that needs to be fleshed out
	def "Clicking the Quick Message button brings up the Quick Message Dialog with the group prepopulated as recipients"() {
		given:
			createTestSubscriptions() // TODO create a SubscriptionBaseSpec with appropriate test data
		when:
			to PageSubscriptionShow, mySubscription
			waitFor { quickMessageButton.displayed }
			quickMessageButton.click()
		then:
			waitFor { at QuickMessageDialog }
		when:
			compose.textArea << "some test message"
			next.click()
		then:
			waitFor { recipients.displayed }
			// TODO: appropriate group checkbox is ticked
			// TODO: recipient count matches number of contacts in group
	}

	def 'Deleting a group that is used in a subscription should fail with an appropriate error'(){
		given:
			def friendsGroup = new Group(name: "Friends").save()
			def subscription = new Subscription(group:friendsGroup, name:"sign-me-up") // TODO populate with appropriate args
		when:
			to PageContactShow, friendsGroup
		then:
			waitFor { header.groupHeaderSection.displayed }
		when:
			header.moreGroupActions.value("delete").click()
		then:
			waitFor{ at DeleteGroupPopup }
		when:
			warningMessage == 'Are you sure you want to delete Friends? WARNING: This cannot be undone'
			ok.jquery.trigger("click")
		then:
			at PageContactShow
			bodyMenu.groupSubmenuLinks.contains("Friends")
			notifications.flashMessagesText.contains("Cannot delete group Friends: is used by sign-me-up Subscription")
	}

	def "clicking the rename option opens the rename small popup"(){
		when:
			to PageSubscriptionView, Subscription.findBy....
		then:
			waitFor { subHead.displayed }
		when:
			header.moreAction("rename").jquery.click()
		then:
			waitFor { at RenameDialog }
			waitFor { something in the RenameDialog is displayed }
	}

	def "clicking the delete option opens the confirm delete small popup"(){
		when:
			to PageSubscriptionView, Subscription.findBy....
		then:
			waitFor { subHead.displayed }
		when:
			header.moreAction("delete").jquery.click()
		then:
			waitFor { at ConfirmDeleteDialog }
	}

	def "clicking the export option opens the export dialog"(){
		when:
			to PageSubscriptionView, Subscription.findBy....
		then:
			waitFor { subHead.displayed }
		when:
			header.moreAction("export").jquery.click()
		then:
			waitFor { at ExportDialog }
	}

	def "selecting a single message reveals the single message view"(){
		setup:
			createSubcriptionMessages()
		when:
			to PageSubscriptionView, Subscription.findBy....
		then:
			waitFor { messageList.messages.displayed }
		when:
			messageList.messages[0].checkbox.click()
		then:
			waitFor { singleMessageDetails.displayed }
			waitFor { singleMessageDetails.text == "some text" }
	}

	def "selecting multiple messages reveals the multiple message view"(){
		setup:
			createSubcriptionMessages()
		when:
			to PageSubscriptionView, Subscription.findBy....
		then:
			waitFor { messageList.messages.displayed }
		when:
			messageList.messages[0].checkbox.click()
			waitFor { singleMessageDetails.displayed }
			messageList.messages[1].checkbox.click()		
		then:
			waitFor { multipleMessageDetails.displayed }
			waitFor { multipleMessageDetails.text.toLowerCase() == "2 messages selected" }
	}

	def "clicking on a message reveals the single message view with clicked message"(){
		setup:
			createSubcriptionMessages()
		when:
			to PageSubscriptionView, Subscription.findBy....
		then:
			waitFor { messageList.messages.displayed }
		when:
			messageList.messages[0].click()
		then:
			waitFor { messageList.displayed }
			messageList.messages[0].hasClass("selected")
			singleMessageDetails.displayed
			singleMessageDetails.text == "some text"
	}

	def "delete single message action works "(){
		setup:
			createSubcriptionMessages()
		when:
			to PageSubscriptionView, Subscription.findBy...
		then:
			waitFor { messageList.messages.displayed }
		when:
			messageList.messages[0].click()
		then:
			waitFor { singleMessageDetails.displayed }
		when:
			singleMessageDetails.delete.click()
		then:
			waitFor { messageList.messages.displayed }
			!messageList.messages*.text.contains("the text contents of the previous message")
	}

	def "delete multiple message action works for multiple select"(){
		setup:
			createSubcriptionMessages()
		when:
			to PageSubscriptionView, Subscription.findBy....
		then:
			waitFor { messageList.messages.displayed }
		when:
			messageList.messages[0].checkbox.click()
			waitFor {singleMessageDetails.displayed }
			messageList.messages[1].checkbox.click()
		then:
			waitFor { multipleMessageDetails.displayed }
		when:
			multipleMessageDetails.deleteAll.click()
		then:
			waitFor { messageList.messages.displayed }
			!messageList.messages*.text.containsAll("the text contents of the previous message", "the second message message")
	}

	def "move single message action works"(){
		setup:
			createSubcriptionMessages()
		when:
			to PageSubscriptionView, Subscription.findBy....
		then:
			waitFor { messageList.messages.displayed }
		when:
			messageList.messages[0].click()
		then:
			waitFor { singleMessageDetails.displayed }
		when:
			singleMessageDetails.moveTo("Activity Awesome").click()
		then:
			waitFor { messageList.messages.displayed }
			!messageList.messages*.text.contains("the text contents of the previous message")
		when:
			to PageActivity, Activity.findBy....
		then:
			waitFor { messageList.messages.displayed }
			messageList.messages*.text.contains("the text we just moved")
	}

	def "move multiple message action works"(){
		setup:
			createSubcriptionMessages()
		when:
			to PageSubscriptionView, Subscription.findBy....
		then:
			waitFor { messageList.messages.displayed }
		when:
			messageList.messages[0].checkbox.click()
			waitFor {singleMessageDetails.displayed }
			messageList.messages[1].checkbox.click()
		then:
			waitFor { multipleMessageDetails.displayed }
		when:
			multipleMessageDetails.moveTo("ACtivity Awesome").click()
		then:
			waitFor { messageList.messages.displayed }
			!messageList.messages*.text.containsAll("the text contents of the previous message", "the second message message")
		when:
			to PageActivity, Activity.findBy....
		then:
			waitFor { messageList.messages.displayed }
			messageList.messages*.text.containsAll("the text we just moved", "the second message we just moved")
	}

	def "moving a message from another activity to a subscription opens the categorise popup for the chosen subscription"(){
		setup:
			def activity = Activity.findBy....
			def m = Fmessage.findBy.....
		when:
			to PageActivity, activity.id, m.id
		then:
			waitFor { singleMessageDetails.displayed }
		when:
			singleMessageDetails.moveTo("My Subscription").click()
		then:
			waitFor { messageList.messages.displayed }
		when:
			to PageMessageSubscription, Subscription.findByName("My Subscription")
		then:
			waitFor { messageList.messages.displayed }
			messageList.messages*.text.contains(m.text)
	}
}