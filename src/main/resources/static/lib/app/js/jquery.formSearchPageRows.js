(function($) {
	
	var methods = {
			
			init	:	function(_options) {
				return this.each(function() {
					
					var options = $.extend({}, $.fn.formSearchPageRows.defaults, _options);
					$(this).data('formSearchPageRows_options', options);
					
					var table = $(options.table);
					var tbody = table.children('tbody');
					if (tbody.length == 0) {
						tbody = $('<tbody></tbody>');
						tbody.appendTo(table);
					}
					
					var triggerSelect = $.proxy(function() {
						var objects = [];
						tbody.children('tr.selected').each(function(i, n) {
							objects.push($(n).data('obj'));
						});
						options.select.call(this, objects);
					}, this);
					
					var generateRow = function(obj, seq) {
						var tr = $('<tr></tr>');
						$.each(options.cm, function(i, c) {
							var td = $('<td class="'+ (c.className||'') +'"></td>').appendTo(tr);
							
							// function
							if ($.isFunction(c.col)) {
								var result = c.col.call(table, obj, td, tr);
								if ($.isArray(result)) {
									$.each(result, function(j, item) {
										td.append(item);
									});
								} else {
									td.append(result);
								}
							}
							
							// String ???
							else {
								var text = '',title='';
								if ('#' == c.col) {
									text = seq;
									td.css({'fontSize': '0.75rem', 'color': '#aaa'});
								} else {
									try {
										text = eval('obj.' + c.col);
										title = eval('obj.' + c.col)
									} catch(e) {
										console.error(e);
									}
								}
								td.attr('title',title)
								td.text(text);
							}
							
						});
						
						tr.appendTo(tbody).data({'obj': obj}).on('mousedown', function(e) {
							if (e.which == 1) {
								if (e.shiftKey) {
									tbody.disableSelection();
								}
							}
						}).on('mouseup', function(e) {
							if (e.which == 1) {
								if (e.shiftKey) {
									tbody.disableSelection();
									var f = tbody.children('tr').index(tbody.children('tr.selected').first());
									var l = tbody.children('tr').index(tbody.children('tr.selected').last());
									var i = tbody.children('tr').index(e.currentTarget);
									if (f == -1) {f = i;}
									if (l == -1) {l = i;}
									var start = Math.min(f, l);
									start = Math.min(start, i);
									var end = Math.max(f, l);
									end = Math.max(end, i);
									for (var i=start; i<=end; i++) {$('tr', tbody).eq(i).addClass('selected');}
									setTimeout(function(){tbody.enableSelection();}, 200);
								} else if (e.ctrlKey) {
									$(e.currentTarget).toggleClass('selected');
								} else {
									$(e.currentTarget).siblings().removeClass('selected');
									$(e.currentTarget).addClass('selected');
								}
								triggerSelect();
							}
						});
					};
					
					$(this).on('submit', function() {
						table.fadeTo('fast', 0.35);
					});
					
					$(this).formSearchPage({
						url				:	options.url,
						data			:	options.data,
						
						pagination		:	options.pagination,
						paginationInfo	:	options.paginationInfo,
						pageNum			:	options.pageNum,
						pageSize		:	options.pageSize,
						
						success	:	$.proxy(function(rows, pagination) {
							if (options.cm != null) {
								
								table.stop(true, true).fadeTo('fast', 1);
								
								tbody.empty();
								var seq = (pagination.pageNum - 1) * pagination.pageSize + 1;
								$.each(rows, function(i, row) {
									generateRow(row, seq + i);
								});
								if (rows.length == 0) {
									$('<tr><td colspan="'+ options.cm.length +'" align="center" class="text-muted">暂无数据！</td></tr>').appendTo(tbody);
								}
								options.success.call(this, rows, pagination);
							}
						}, this)
					});
					
				});
			},
			
			search	:	function(_params) {
				$(this).formSearchPage('search', _params);
			}
			
	};
	
	
	
	$.fn.formSearchPageRows = function(method) {
		if (methods[method]) {
			return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
		} else if (typeof method === 'object' || !method) {
			return methods.init.apply(this, arguments);
		} else {
			$.error('Method ' + method + ' does not exist on jQuery.formSearchPageRows');
		}
	};
	
	$.fn.formSearchPageRows.defaults = {
			url				:	'/form-search-page-rows',
			data			:	{},
			
			pagination		:	'.pagination',
			paginationInfo	:	'.pagination-info',
			pageNum			:	1,
			pageSize		:	10,
			
			table			:	'.table',
			cm				:	null,
			select			:	$.noop,
			
			success			:	$.noop
    };
	
})(jQuery);


