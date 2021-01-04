$(document).ready(function() {
	
	// css var
	cssVars({});
	
	// nav-wrapper
	(function() {
		// active
		$('a[data-toggle="collapse"]', '.navbar-wrapper').on('click', function() {
			if ($($(this).attr('href')).hasClass('show')) {
				$(this).parent().removeClass('active');
			} else {
				$(this).parent().addClass('active');
			}
		});
	})();
	
	// 顶部菜单左右移动按钮
	(function() {
		$("body").on('click','[data-stopPropagation]',function (e) {
	        e.stopPropagation();
	    });
	})();
	
	
	// left menu toggle
	(function() {
		$('.left-nav-toggle').on('click', function() {
			var cookieName = 'leftNavHidden';
			var leftNavHiddenClass = 'left-nav-hidden';
			if ($('body').hasClass(leftNavHiddenClass)) {
				$('body').removeClass(leftNavHiddenClass);
				$.cookie(cookieName, null, {path: '/'});
			} else {
				$('body').addClass(leftNavHiddenClass);
				$.cookie(cookieName, 'yes', {path: '/'});
			}
		});
	})();

});