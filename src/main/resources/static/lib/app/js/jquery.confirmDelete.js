(function($) {
	
	$.extend({
		confirmDelete : function() {
			
			var methods = {
					
					init	:	function() {
						
						var url = '';
						var success = null;
						var fail = null;
						
						$.each(arguments, function(i, arg) {
							if (i == 0) {
								url = arg;
							} else {
								if ($.isFunction(arg)) {
									if (success == null) {
										success = arg;
									} else if (fail == null) {
										fail = arg;
									}
								}
							}
						});
						
						$.confirm('确认删除？', function() {
							$.ajax({
								type	:	'delete',
								url		:	$.isFunction(url) ? url() : url,
							}).done(function() {
								if ($.isFunction(success)) {
									success.apply(this, arguments);
								}
							}).fail(function() {
								if ($.isFunction(fail)) {
									fail.apply(this, arguments);
								}
							});
						});
						
					}
					
			};
			
			return methods.init.apply(window, arguments);
			
	    }
	});
	
})(jQuery);


