$(function(){
	$("#sendBtn").click(send_letter);
	$(".close").click(delete_msg);
	$("#deleteLetter").click(delete_letter);
});

function send_letter() {
	$("#sendModal").modal("hide");
	var toName = $("#recipient-name").val();
	var content = $("#message-text").val();
	$.post(
		CONTEXT_PATH+"/letter/send",
		{"toName":toName,"content":content},
		function (data) {
			if(data.code==0){
				$("hintModalLabel").text(data.msg);
			}else{
				$("hintModalLabel").text("发送成功");
			}
			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				location.reload();
			}, 2000);

		}
	)

}

function delete_msg() {
	// TODO 删除数据
	$(this).parents(".media").remove();
}

function delete_letter() {
	var letterId=$("#letterId").val();
	$.get(
		CONTEXT_PATH+"/letter/delete",
		{"letterId":letterId},
		function (data) {
			location.reload();
		}
	)
}