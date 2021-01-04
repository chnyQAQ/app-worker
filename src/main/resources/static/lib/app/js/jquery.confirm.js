(function($) {
	
	$.extend({
		confirm : function() {
			
			var methods = {
					
					init	:	function() {
						
						var title = '确认';
						var text = '';
						var confirmAction = null;
						var cancelAction = null;
						
						$.each(arguments, function(i, arg) {
							if (i == 0) {
								text = arg;
							} else {
								if ($.isFunction(arg)) {
									if (confirmAction == null) {
										confirmAction = arg;
									} else if (cancelAction == null) {
										cancelAction = arg;
									}
								}
							}
						});
						
						$('.globalExclusiveModal').modal('hide');
						
						var str = '<div class="modal fade globalExclusiveModal confirmModal" tabindex="-1" data-backdrop="static">'
									+ '<div class="modal-dialog modal-dialog-centered" role="document" tabindex="-1" data-backdrop="static" style="max-width: 500px;">'
										+ '<div class="modal-content">'
											+ '<div class="modal-header">'
												+ '<h5 class="modal-title text-muted"><i class="fas fa-question-circle"></i> '+ title +'</h5>'
												+ '<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>'
											+ '</div>'
											+ '<div class="modal-body">'
												+ '<h4 class="text-center" style="word-wrap: break-word;">'+ text +'</h4>'
											+ '</div>'
											+ '<div class="modal-footer">'
												+ '<button type="button" class="btn btn-sm btn-danger btn-confirm">确认</button>'
												+ '<button type="button" class="btn btn-sm btn-secondary btn-cancel">取消</button>'
											+ '</div>'
										+ '</div>'
									+ '</div>'
							+ '</div>';
						
						var $confirm = $(str).appendTo('body').modal('show').on('shown.bs.modal', function() {
							$('.btn-confirm', this).trigger('focus');
						}).on('hidden.bs.modal', function() {
							$confirm.modal('dispose').remove();
						});
						
						var $cancelButton = $('.btn-cancel', $confirm);
						var $confirmButton = $('.btn-confirm', $confirm);
						
						$cancelButton.on('click', function() {
							$cancelButton.prop('disabled', true);
							$confirmButton.prop('disabled', true);
							if (cancelAction == null) {
								$confirm.modal('hide');
							} else {
								cancelAction();
							}
						});
						
						$confirmButton.on('click', function() {
							$cancelButton.prop('disabled', true);
							$confirmButton.prop('disabled', true);
							if (confirmAction == null) {
								$confirm.modal('hide');
							} else {
								confirmAction();
								$confirm.modal('hide');
							}
						});
						
					},
					
					close	:	function() {
						$('.confirmModal').modal('hide');
					}
					
			};
			
			if (arguments.length == 1 && arguments[0] == 'close') {
				methods.close();
			} else {
				methods.init.apply(window, arguments);
			}
			
			
	    }
	});
	
})(jQuery);


