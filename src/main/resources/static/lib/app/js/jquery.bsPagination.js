(function($) {
	
	var methods = {
		
			init		:	function(_options) {
				return this.each(function() {
					
					var options = $.extend({}, $.fn.bsPagination.defaults, _options);
					$(this).data('bsPagination_options', options);
					
					$(this).empty();
					
					// current
					$('<li class="page-item active"><a class="page-link" href="#" data-page='+ options.pageNum +' title="第'+ options.pageNum +'页">'+ options.pageNum +'</a></li>').appendTo(this);
					
					var beforeNum = options.pageNum;
					var afterNum = options.pageNum;
					
					var addPageAfter = $.proxy(function() {
						afterNum = afterNum + 1;
						if (afterNum <= options.pages) {
							$('<li class="page-item"><a class="page-link" href="#" data-page='+ afterNum +' title="第'+ afterNum +'页">'+ afterNum +'</a></li>').appendTo(this);
						}
					}, this);
					
					var addPageBefore = $.proxy(function() {
						beforeNum = beforeNum - 1;
						if (beforeNum >= 1) {
							$('<li class="page-item"><a class="page-link" href="#" data-page='+ beforeNum +' title="第'+ beforeNum +'页">'+ beforeNum +'</a></li>').prependTo(this);
						}
					}, this);
					
					for (var i = 1; i < options.numberOfPages; i++) {
						if (beforeNum == 1) {
							addPageAfter();
							continue;
						}
						if (afterNum == options.pages) {
							addPageBefore();
							continue;
						}
						if (i % 2 == 0) {
							addPageBefore();
						} else {
							addPageAfter();
						}
					}
					
					// prev, first
					if (options.pageNum > 1) {
						$('<li class="page-item"><a class="page-link" href="#" data-page='+ (options.pageNum - 1) +' title="上页">上页</a></li>').prependTo(this);
						$('<li class="page-item"><a class="page-link" href="#" data-page='+ 1 +' title="首页">首页</a></li>').prependTo(this);
					} else {
						$('<li class="page-item disabled"><a class="page-link" href="#" data-page="" title="上页">上页</a></li>').prependTo(this);
						$('<li class="page-item disabled"><a class="page-link" href="#" data-page="" title="首页">首页</a></li>').prependTo(this);
					}
					
					// next, last
					if (options.pageNum < options.pages) {
						$('<li class="page-item"><a class="page-link" href="#" data-page='+ (options.pageNum + 1) +' title="下页">下页</a></li>').appendTo(this);
						$('<li class="page-item"><a class="page-link" href="#" data-page='+ options.pages +' title="尾页">尾页</a></li>').appendTo(this);
					} else {
						$('<li class="page-item disabled"><a class="page-link" href="#" data-page="" title="下页">下页</a></li>').appendTo(this);
						$('<li class="page-item disabled"><a class="page-link" href="#" data-page="" title="尾页">尾页</a></li>').appendTo(this);
					}
					
					$('a', this).on('click', $.proxy(function(e) {
						e.preventDefault();
						e.stopPropagation();
						var page = $(e.target).data('page');
						if (page) {
							$('li.active', this).removeClass('active');
							$(e.target).parent().addClass('active');
							options.onPageClicked.call(this, page);
						}
					}, this));
					
				});
			},
		
			getPageNum	:	function() {
				return $('li.active', this).children('a').data('page');
			}
		
	};
	
	$.fn.bsPagination = function(method) {
		if (methods[method]) {
			return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
		} else if (typeof method === 'object' || !method) {
			return methods.init.apply(this, arguments);
		} else {
			$.error('Method ' + method + ' does not exist on jQuery.bsPagination');
		}
	};
	
	$.fn.bsPagination.defaults = {
			pages			:	1,
			pageNum			:	1,
			numberOfPages	:	5,
			onPageClicked	:	$.noop
    };
	
})(jQuery);


