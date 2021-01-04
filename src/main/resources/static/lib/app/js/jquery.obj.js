(function($) {
	
	$.extend({
		obj : function(_params) {
			
			var params = {};
			
			if (typeof(_params) != 'undefined' && _params != null) {
				if ($.isFunction(_params)) {
					params = _params();
					if (typeof(params) == 'undefined' || params == null || !$.isPlainObject(params)) {
						params = {};
					}
				} else if ($.isPlainObject(_params)) {
					params = _params;
				}
			}
			
			return params;
			
	    }
	});
	
})(jQuery);


