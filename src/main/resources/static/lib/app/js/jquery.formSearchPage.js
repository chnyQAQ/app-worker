(function($) {
	
	
	var methods = {
		
		init		:	function(_options) {
			return this.each(function() {
				
				var options = $.extend({}, $.fn.formSearchPage.defaults, _options);
				$(this).data('formSearchPage_options', options);
				
				var doPagination = $.proxy(function(pagination) {
					
					var total = pagination.total;
					var pageNum = pagination.pageNum;
					var pageSize = pagination.pageSize;
					var pages = Math.ceil(total / pageSize) || 1;
					
					var from = (pageNum - 1) * pageSize + 1;
					var to = from + pagination.rows.length - 1;
					if (to == 0) {
						from = 0;
					}
					$(options.paginationInfo).text('共 ' + total + ' 条记录');
					$(options.pagination).bsPagination({
						pages			:	pages,
						pageNum			:	pageNum,
						onPageClicked	:	$.proxy(function() {
							methods.search.call(this);
						}, this)
					});
					
				}, this);
				
				$(this).on('submit', function() {
					$(options.paginationInfo).fadeTo('fast', 0.35);
					$(options.pagination).fadeTo('fast', 0.35);
				});
				
				$(this).formSearch({
					url		:	options.url,
					data	:	$.extend({}, $.obj(options.data), {pageNum: options.pageNum, pageSize: options.pageSize}),
					success	:	$.proxy(function(pagination) {
						
						$(options.paginationInfo).stop(true, true).fadeTo('fast', 1);
						$(options.pagination).stop(true, true).fadeTo('fast', 1);
						
						doPagination(pagination);
						options.success.call(this, pagination.rows, pagination);
					}, this)
				});
				
			});
		},
		
		search		:	function(_params) {
			
			var options = $(this).data('formSearchPage_options');
			var pageNum = $(options.pagination).bsPagination('getPageNum');
			
			$(this).formSearch('search', $.extend({}, {pageNum: pageNum}, $.obj(_params)));
			
		}
		
	};
	
	
	$.fn.formSearchPage = function(method) {
		if (methods[method]) {
			return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
		} else if (typeof method === 'object' || !method) {
			return methods.init.apply(this, arguments);
		} else {
			$.error('Method ' + method + ' does not exist on jQuery.formSearchPage');
		}
	};
	
	$.fn.formSearchPage.defaults = {
		url				:	'/form-search-page',
		data			:	{},
		
		pagination		:	'.pagination',
		paginationInfo	:	'.pagination-info',
		pageNum			:	1,
		pageSize		:	10,
		
		success			:	$.noop
    };
	
})(jQuery);


