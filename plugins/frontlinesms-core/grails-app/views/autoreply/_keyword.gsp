<div class="input">
	<label for="keyword"><g:message code="autoreply.keyword.title"/></label>
	<g:textField name="keyword" value="${activityInstanceToEdit?.keyword?.value}" disabled="${activityInstanceToEdit?.keyword?.value == ''}"/>
</div>
<div class="input optional">
	<label for="blankKeyword"><g:message code="autoreply.all.messages"/></label>
	<g:checkBox name="blankKeyword" checked="${activityInstanceToEdit?.keyword?.value == ''}"/>
</div>
<r:script>
$(function() {
	$('#blankKeyword').live("change", function() {
		if($(this).is(":checked")) {
			$("#keyword").attr("disabled", "disabled");
			$("#keyword").removeClass("required error");
			$(".error").hide();
		} else {
			$("#keyword").attr("disabled", false);
			$("#keyword").addClass("required");
		}
	});
});
</r:script>
