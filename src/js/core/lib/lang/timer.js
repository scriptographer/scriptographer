new function() {
	var timers = {}, current = 1;
	function createTimer(func, delay, periodic) {
		var timer = new Timer(delay || 0, periodic);
		timers[timer.id] = timer;
		timer.onExecute = typeof func == 'string' ? new Function(func) : func;
		return timer.id;
	}

	global.setTimeout = function(func, delay) {
		return createTimer(func, delay, false);
	}

	global.setInterval = function(func, delay) {
		return createTimer(func, delay, true);
	}

	global.clearTimeout = global.clearInterval = function(id) {
		var timer = timers[id];
		if (timer) {
			timer.abort();
			delete timers[id];
		}
	}

	global.sleep = function(delay) {
		java.lang.Thread.sleep(delay);
	}
}

Function.inject(new function() {

	function timer(periodic) {
		return function(delay, bind, args) {
			var func = this.wrap(bind, args);
			if (delay === undefined)
				return func();
			var timer = new Timer(delay, periodic);
			timer.onExecute = func;
			func.clear = function() {
				timer.abort();
			};
			func.isValid = function() {
				return timer.isValid();
			}
			return func;
		};
	}

	return {
		generics: true,

		delay: timer(false),
		periodic: timer(true)
	}
});
