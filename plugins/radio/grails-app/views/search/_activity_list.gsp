<li class='field'>
	<g:select class="dropdown" name="activityId" from="${activityInstanceList + folderInstanceList + radioShowInstanceList}"
			  value="${search?.activityId}"
			  optionKey="${{(it instanceof frontlinesms2.Poll ? 'poll' : 'folder') + '-' + it.id}}"
			  optionValue="${{it.name}}"
			  noSelection="${['':'Select activity/folder/RadioShow']}"/>
</li>