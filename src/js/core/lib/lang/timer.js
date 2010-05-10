new function() {
	var timers = {}, current = 1;
	function createTimer(func, delay, periodic) {
		var id = current++;
		var timer = timers[id] = new Timer(delay || 0, periodic);
		timer.onExecute = typeof func == 'string' ? new Function(func) : func;
		timer.start();
		return id;
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
			timer.dispose();
			delete timers[id];
		}
	}

	global.sleep = function(delay) {
		java.lang.Thread.sleep(delay);
	}
}

Function.inject(new function() {
	function timer(that, periodic, delay, bind, args) {
		if (delay === undefined)
			return that.apply(bind, args ? args : []);
		var fn = that.wrap(bind, args);
		var timer = new Timer(delay, periodic);
		fn.clear = function() {
			timer.dispose();
		};
		timer.onExecute = fn;
		timer.start();
		return fn;
	}

	return {
		generics: true,

		delay: function(delay, bind, args) {
			return timer(this, false, delay, bind, args);
		},

		periodic: function(delay, bind, args) {
			return timer(this, true, delay, bind, args);
		}
	}
});
