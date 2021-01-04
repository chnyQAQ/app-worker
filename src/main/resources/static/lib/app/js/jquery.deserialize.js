jQuery.fn.extend({
	
	deserializeJSON : function(json) {
		
		var parseObjectIntoNameValuePairs =function(object,root) {
			if(!root) root = "";
			//Literal in array
			if(_.isString(object) || _.isNumber(object)) {
				var v = {};
				path = root;
				v[path] = object;
				return v;
			}
			var results = _.map(object, function(val,key) {
				
				//Literal
				if(_.isString(val) || _.isNumber(val) || _.isBoolean(val)) {
					var v = {};
					if(root != "")
						path = root + "[" + key + "]";
					else
						path = key;
					if (_.isBoolean(val)) {
						val = val ? 'true' : 'false';
					}
					v[path] = val;
					return v;
				}
				
				//Array
				else if(_.isArray(val)) {
					return _.map(val, function(v,k) {
						var path;
						if(root != "")
							path = root + "[" + key + "]" + "[" + k + "]";
						else
							path = key + "[" + k + "]";
						return parseObjectIntoNameValuePairs(v,path);
					});
				}
				
				//Object
				else if(_.isObject(val)) {
					var path;
					if(root != "")
						path = root + "[" + key + "]";
					else
						path = key;
					return parseObjectIntoNameValuePairs(val,path);
				}
				
				//We don't care about it
				else {
					return undefined;
				}
			});
			
			//remove heirarchy and compactify
			return _.flatten(_.filter(results, function(val){return val != undefined}));
			
		};
		
		var valuePairs = parseObjectIntoNameValuePairs(json);
		
		_.each(valuePairs, $.proxy(function(val) {
			var vK = _.first(_.keys(val));
			var target = $('[name="'+vK+'"]', this);
			target.val(val[vK]).attr('data-value', val[vK]).trigger('change');
			
			if (target.attr('data-dict')) {
				var tagName = target.get(0).tagName;
				if (!(tagName == 'SELECT' || tagName == 'select' || (target.children('select').length > 0))) {
					target.dictionary();
				}
			}
			
		}, this));
		
	}

});