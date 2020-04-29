$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");

	var title = $("#recipient-name").val();
	var content = $("#message-text").val();
	// alert("进入到publish函数中");
	// alert("title="+title);
	// alert("content="+content);
	// alert("path="+CONTEXT_PATH+"/discuss/add");
	$.post(
		CONTEXT_PATH+"/discuss/add",
		{"content":content,"title":title},
		function (data) {
			data = $.parseJSON(date);
			$("#hintModal").text(data.msg);
			//显示提示框
			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
			}, 2000);
		}
	)

}