function chooseActivity() {
	var activity = $("input[name='activity']:checked").val();
	var activityUrl = activity.replace(/\s/g, "") + '/create';
	var title = i18n("wizard.title.new") + activity;
	$(this).dialog('close');
	$.ajax({
		type:'GET',
		dataType: "html",
		url: url_root + activityUrl,
		beforeSend: function(){ showThinking(); },
		success: function(data, textStatus) { hideThinking(); launchMediumWizard(title, data, i18n('wizard.create'), 675, 500, false); }
	});
	return;
}
	
function checkForSuccessfulSave(response, type) {
	$("#submit").removeAttr('disabled');
	if (response.ok) {
		$("div.confirm").parent().hide();
		$(".ui-tabs-nav").hide();
		$("div.summary").show();
		$(".summary #activityId").val(response.ownerId);
		var messageDialog = $("#modalBox");
		messageDialog.dialog(
			{
				modal: true,
				title: i18n("popup.title.saved", type),
				buttons: [
					{ text:i18n("action.cancel"), click: cancel, id:"cancel" },
					{ text:i18n("action.back"), disabled: "true"},
					{ text:i18n('action.ok'),  click: summaryRedirect, id:"submit" }],
				close: function() { $(this).remove(); }
			}
		);
		messageDialog.css("height", "389px");
		
	} else {
		var errors = $(".error-panel");
		errors.text(response.text);
		errors.show();
		$("#submit").removeAttr('disabled');
	}
}
	
function summaryRedirect() {
	var activityId = $(".summary #activityId").val();
	$(this).dialog('close');
	window.location.replace(url_root + "message/activity/" + activityId	);
}
