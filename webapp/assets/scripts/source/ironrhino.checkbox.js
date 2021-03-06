(function($) {

	function check(group) {
		var boxes = $('input[type=checkbox]:not(.normal):not(.checkall)', group);
		var allchecked = boxes.length > 0;
		if (allchecked)
			for (var i = 0; i < boxes.length; i++)
				if (!boxes[i].checked) {
					allchecked = false;
					break;
				}
		$('input.checkall[type=checkbox]:not(.normal)', group).prop('checked',
				allchecked);
	}

	$.fn.checkbox = function() {
		$('.checkboxgroup', this).each(function() {
					check(this);
				});

		$('input[type=checkbox]', this).click(function(event) {
			if ($(this).hasClass('normal'))
				return;
			var group = $(this).closest('.checkboxgroup');
			if (!group.length)
				group = $(this).closest('form.richtable');
			if (!group.length)
				group = $(this).closest('div.controls');
			if ($(this).hasClass('checkall')) {
				var b = this.checked;
				if (group.length)
					$('input[type=checkbox]:not(.normal)', group).each(
							function() {
								this.checked = b;
								var tr = $(this).closest('tr');
								if (tr.length) {
									if (b)
										tr.addClass('selected');
									else
										tr.removeClass('selected');
								}
							});
			} else {
				try {
					document.getSelection().removeAllRanges();
				} catch (e) {
				}
				if (!event.shiftKey) {
					var tr = $(this).closest('tr');
					if (tr) {
						if (group.length && this.checked)
							tr.addClass('selected');
						else
							tr.removeClass('selected');
					}
					var table = $(this).closest('table');
					if (table.hasClass('treeTable')) {
						var checked = this.checked;
						$('tr.child-of-node-' + this.value, table)
								.find('input[type=checkbox]').prop('checked',
										checked).end().each(function() {
											if (checked)
												$(this).addClass('selected');
											else
												$(this).removeClass('selected');
										});
					}
				} else if (group.length) {
					var boxes = $(
							'input[type=checkbox]:not(.checkall):not(.normal)',
							group);
					var start = -1, end = -1, checked = false;
					for (var i = 0; i < boxes.length; i++) {
						if ($(boxes[i]).hasClass('lastClicked')) {
							checked = boxes[i].checked;
							start = i;
						}
						if (boxes[i] == this) {
							end = i;
						}
					}
					if (start > end) {
						var tmp = end;
						end = start;
						start = tmp;
					}
					if (start >= 0 && end > start)
						for (var i = start; i <= end; i++) {
							boxes[i].checked = checked;
							tr = $(boxes[i]).closest('tr');
							if (tr) {
								if (boxes[i].checked)
									tr.addClass('selected');
								else
									tr.removeClass('selected');
							}
						}
				}
				$('input[type=checkbox]', group).removeClass('lastClicked');
				$(this).addClass('lastClicked');
				check(group);
			}
		});
		return this;
	}
})(jQuery);

Observation.checkbox = function(container) {
	$(container).checkbox();
};