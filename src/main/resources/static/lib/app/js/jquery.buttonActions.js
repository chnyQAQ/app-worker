(function($) {
	
	$.extend({
		buttonActions : function(actions, context) {
			
			var toolbar = $('<div class="btn-toolbar d-inline-flex"></div>');
			var group = $('<div class="btn-group btn-group-sm"></div>');
			
			$.each(actions, function(i, action) {
				if (action.text == '-' || action.text == '|') {
					group.addClass('mr-2').appendTo(toolbar);
					group = $('<div class="btn-group btn-group-sm"></div>');
				} else {
					if (action.href) {
						$('<a class="btn btn-outline-light">'+ action.text +'</a>').attr('href', action.href).appendTo(group);
					} else if (action.click) {
						$('<button type="button" class="btn btn-outline-light">'+ action.text +'</button>').appendTo(group).on('click', function(e) {
							action.click.call(context, context);
						});
					} else {
						$('<button type="button" class="btn btn-outline-light">'+ action.text +'</button>').appendTo(group);
					}
				}
			});
			group.appendTo(toolbar);
			
			return toolbar;
	    }
	});
	
})(jQuery);


