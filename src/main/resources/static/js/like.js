$(function(){
	$("#postTopBtn").click(setTop);
	$("#postWonderfulBtn").click(setWonderful);
	$("#postDeleteBtn").click(setDelelte);

	function setTop() {
		$.post(
			CONTEXT_PATH+"/discuss/top",
			{"id":$("#postId").val()},
			function (data) {
				data = $.parseJSON(data);
				if(data.code==0){
					$("#postTopBtn").attr("disabled","disabled");
				}else{
					alert(data.msg);
				}
			}
		)
	}

	function setWonderful() {
		$.post(
			CONTEXT_PATH+"/discuss/wonderful",
			{"id":$("#postId").val()},
			function (data) {
				data = $.parseJSON(data);
				if(data.code==0){
					$("#postWonderfulBtn").attr("disabled","disabled");
				}else{
					alert(data.msg);
				}
			}
		)
	}

	function setDelelte() {
		$.post(
			CONTEXT_PATH+"/discuss/delete",
			{"id":$("#postId").val()},
			function (data) {
				data = $.parseJSON(data);
				if(data.code==0){
					location.href=CONTEXT_PATH+"/index";
				}else{
					alert(data.msg);
				}
			}
		)
	}

})


function like(btn,entityType,entityId,entityUserId,postId) {
	$.post(
		CONTEXT_PATH+"/like",
		{"entityType":entityType,"entityId":entityId,"entityUserId":entityUserId,"postId":postId},
		function (data) {
			data = $.parseJSON(data);
			if(data.code==0){
				$(btn).children("i").text(data.likeCount);
				$(btn).children("b").text(data.likeStatus==1?'已赞':'赞');
			}else{
				alert(data.msg);
			}
		}
	)
}