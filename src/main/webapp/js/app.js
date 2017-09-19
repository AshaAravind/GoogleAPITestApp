$(document).ready(function()    {
	$("#calendar-form").submit(function(e) {
		e.preventDefault();
		var formdata = $('#calendar-form').serialize();
		$.ajax({
			url: "tokentest?"+formdata, 
			success: function(data, msg){
				$('#calendar-form').hide();
				
				$('.success-message').removeAttr('hidden');
			},
			error: function(error){
				if(error.status == 407){
					$('#authFailedModal').modal();
				} else {
					alert('Could not add event! Unexpected error!!');
				}
				
			}
		});
	});
	
	$('.login').on('click', function(){
		window.location = 'http://localhost:8080/GoogleAPITestApp/signin';
	});

});