(function($) {
	
	var methods = {
			
			init		:	function() {
				return this.each(function() {
					methods.clean.call(this);
				});
			},
			
			clean	:	function() {
				$('.is-invalid', this).removeClass('is-invalid');
				$('.valid-feedback', this).remove();
				$('.invalid-feedback', this).remove();
				$('.valid-tooltip', this).remove();
				$('.invalid-tooltip', this).remove();
			},
			
			show	:	function(errors) {
				methods.clean.call(this);
				if ($.isPlainObject(errors)) {
					$.each(errors, $.proxy(function(name, message) {
						
						var target = $('[name="'+ name +'"]', this);
						if (target.length == 1 && target.attr('data-validate-name')) {
							target = $('[name="'+ target.attr('data-validate-name') +'"]', this);
						}
						
						if (target.length == 1) {
							var $feedback = $('<div class="invalid-feedback">'+ message +'</div>');
							if (target.parent() && target.parent().hasClass('input-group')) {
								$feedback.appendTo(target.parent());
							} else if (target.parent() && target.parent().hasClass('select-group')) {
								var childSize = target.parent().children().length;
								$feedback.css('width', (100 / childSize) + '%');
								$feedback.appendTo(target.parent().parent());
							} else {
								$feedback.insertAfter(target);
							}
							
							target.addClass('is-invalid');
						} else {
							$.alert(name + '' +message);
						}
						
					}, this));
				}
			}
	
	};
	
	$.fn.formValidate = function(method) {
		if (methods[method]) {
			return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
		} else if (typeof method === 'object' || !method) {
			return methods.init.apply(this, arguments);
		} else {
			$.error('Method ' + method + ' does not exist on jQuery.formValidate');
		}
	};
	
})(jQuery);


