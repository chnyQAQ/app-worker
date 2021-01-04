(function($) {
	
	var methods = {
			
			init	:	function(_options) {
				return this.each(function() {
					
					var options = $.extend({}, $.fn.formSearchRows.defaults, _options);
					$(this).data('formSearchRows_options', options);
					
					var table = $(options.table);
					var tbody = table.children('tbody');
					if (tbody.length == 0) {
						tbody = $('<tbody></tbody>');
						tbody.appendTo(table);
					}
					
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
								var text = '';
								if ('#' == c.col) {
									text = seq;
									td.css({'fontSize': '0.75rem', 'color': '#aaa'});
								} else {
									try {
										text = eval('obj.' + c.col);
									} catch(e) {
										console.error(e);
									}
								}
								td.text(text);
							}
							
						});
						
						tr.appendTo(tbody);
					};
					
					$(this).on('submit', function() {
						table.fadeTo('fast', 0.35);
					});
					
					$(this).formSearch({
						url				:	options.url,
						data			:	options.data,
						
						success	:	$.proxy(function(rows) {
							if (options.cm != null) {
								
								table.stop(true, true).fadeTo('fast', 1);
								
								tbody.empty();
								$.each(rows, function(i, row) {
									generateRow(row, 1 + i);
								});
								options.success.call(this, rows);
							}
						}, this)
					});
					
				});
			},
			
			search	:	function(_params) {
				$(this).formSearch('search', _params);
			}
			
	};
	
	
	
	$.fn.formSearchRows = function(method) {
		if (methods[method]) {
			return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
		} else if (typeof method === 'object' || !method) {
			return methods.init.apply(this, arguments);
		} else {
			$.error('Method ' + method + ' does not exist on jQuery.formSearchRows');
		}
	};
	
	$.fn.formSearchRows.defaults = {
			url				:	'/form-search-rows',
			data			:	{},
			
			table			:	'.table',
			cm				:	null,
			
			success			:	$.noop
    };
	
})(jQuery);


