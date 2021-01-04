$(document).ready(function() {
	
	// ajax
	(function() {
		$.ajaxSetup({
			cache		:	false,
			processData	:	false
		});
		// 统一错误处理
		$(document).ajaxError(function(event, jqxhr, settings, thrownError) {
			if (jqxhr.responseJSON) {
				var error = jqxhr.responseJSON['error'];
				var message = jqxhr.responseJSON['message'];
				if (error == 'dataValidationError') {
					// 由校验组件处理
				} else {
					$.alert(error, message);
				}
			}
		});
		// ajax预处理
		$.ajaxPrefilter(function( options, originalOptions, jqXHR ) {
			if (typeof(contextPath) != 'undefined' && options.url.indexOf('http') != 0) {
				options.url = contextPath + options.url;
			}
			if (options.type == 'post' || options.type == 'POST' || options.type == 'put' || options.type == 'PUT') {
				if ($.isPlainObject(options.data)) {
					options.data = JSON.stringify(options.data);
					options.contentType = 'application/json;charset=UTF-8';
				}
			} else {
				if (typeof(options.data) != 'undefined' && $.isPlainObject(options.data)) {
					options.data = $.param(options.data, true);
				}
			}
		});
	})();

	// toastr
	(function() {
		toastr.options = {
				"closeButton": true,
				"debug": false,
				"progressBar": true,
				"preventDuplicates": false,
				"positionClass": "toast-top-center",
				"onclick": null,
				"showDuration": "400",
				"hideDuration": "1000",
				"timeOut": "7000",
				"extendedTimeOut": "1000",
				"showEasing": "swing",
				"hideEasing": "linear",
				"showMethod": "fadeIn",
				"hideMethod": "fadeOut"
			};
	})();

});