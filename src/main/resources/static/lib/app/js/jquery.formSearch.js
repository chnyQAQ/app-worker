(function($) {
	
	var methods = {
			
			init		:	function(_options) {
				return this.each(function() {
					
					var options = $.extend({}, $.fn.formSearch.defaults, _options);
					$(this).data('formSearch_options', options);
					
					$(this).on('submit', function(e, _params) {
						
						e.preventDefault();
						
						if ($(this).prop('busy')) {
							return;
						} else {
							$('button[type="submit"]', this).prop('disabled', true);
							$('button[type="submit"]', this).next('button').prop('disabled', true);
							$(this).prop('busy', true);
						}
						$(this).fadeTo('fast', 0.35);
						
						var url = options.url;
						if ($.isFunction(url)) {
							url = url();
						}
						
						var formQueryString = $(this).serialize();
						if (typeof(formQueryString) != 'undefined' && formQueryString != null && formQueryString != '') {
							if (url.indexOf('?') > 0) {
								url = url + '&' + formQueryString;
							} else {
								url = url + '?' + formQueryString;
							}
						}
						
						$.ajax({
							type		:	'get',
							url			:	url,
							data		:	$.extend({}, $.obj(options.data), $.obj(_params))
						}).done(options.success).always($.proxy(function() {
							$(this).stop(true, true).fadeTo('fast', 1);
							$('button[type="submit"]', this).prop('disabled', false);
							$('button[type="submit"]', this).next('button').prop('disabled', false);
							$(this).prop('busy', false);
						}, this));
						
					}).on('reset', function() {
						// 重置时清空所有的表单元素的值（除开input的type为button、submit、reset、hidden的）
						$(':input', $(this)[0])
							.not(':button, :submit, :reset')
							.not(':hidden')
							.val('')
							.removeAttr('checked')
							.removeAttr('selected');
						// 将当前表单所有的行政区划的隐藏域的值清空
						$('input:hidden[class=district]', $(this)[0]).val('');
						$(this).trigger('submit');
					});
				
				});
			},
			
			search	:	function(_params) {
				$(this).trigger('submit', _params);
			}
	
	};
	
	$.fn.formSearch = function(method) {
		if (methods[method]) {
			return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
		} else if (typeof method === 'object' || !method) {
			return methods.init.apply(this, arguments);
		} else {
			$.error('Method ' + method + ' does not exist on jQuery.formSearch');
		}
	};
	
	$.fn.formSearch.defaults = {
			url				:	'/form-search',
			data			:	{},
			success			:	$.noop
    };
	
})(jQuery);


