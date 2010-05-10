Process = Base.extend(new function() {
	function readAll(reader) {
		if (!reader || !reader.ready())
			return null;
		var res = [], line;
		while (reader.ready() && (line = reader.readLine()) != null)
			res.push(line);
		return res.join(java.lang.System.getProperty('line.separator'));
	}

	function updateTimer() {
		if (this.process) {
			var hasCallback = this._onData || this._onError || this._onDone;
			if (!this._timer != !hasCallback) { // xor
				if (hasCallback) {
					this._timer = (function() {
						if (this._onError) {
							var error = this.readError();
							if (error)
								this._onError(error);
						}
						if (this._onData) {
							var data = this.readAll();
							if (data)
								this._onData(data);
						}
						if (!this.isRunning()) {
							if (this._onDone)
								this._onDone();
							this._timer = this._timer.clear();
						}
					}).periodic(1, this);
				} else {
					this._timer = this._timer.clear();
				}
			} 
		}
	}

	var fields = {
		initialize: function(args, directory) {
			this._builder = new java.lang.ProcessBuilder(Array.create(args));
			if (directory)
				this._builder.directory(new File(directory));
			this.environment = this._builder.environment();
			this._onData = null;
			this._onError = null;
			this._timer = null;
		},

		start: function() {
			this.process = this._builder.start();
			this.output = new java.io.PrintStream(this.process.getOutputStream());
			this.input = new java.io.BufferedReader(new java.io.InputStreamReader(this.process.getInputStream()));
			this.error = new java.io.BufferedReader(new java.io.InputStreamReader(this.process.getErrorStream()));
			updateTimer.call(this);
		},

		write: function(str) {
			this.output.print(str);
			this.output.flush();
		},

		writeln: function(str) {
			this.output.println(str);
			this.output.flush();
		},

		readln: function() {
			return reader.ready() && this.input.readLine();
		},

		readAll: function() {
			return readAll(this.input);
		},

		readError: function() {
			return readAll(this.error);
		},

		isRunning: function() {
			try {
				this.process.exitValue();
			} catch (e) {
				return true;
			}
			return false;
		},

		wait: function(timeout) {
			if (timeout == undefined) {
				return this.process.waitFor();
			} else {
				// Wait for the process to finish.
				// Unfortunatelly process.waitFor does not know waitFor(timeout)
				var time = new Date().getTime();
				while(this.isRunning() && new Date().getTime() - time < timeout)
					sleep(Math.min(timeout, 10));
				return this.process.exitValue();
			}
		},

		destroy: function() {
			this.process.destroy();
		},

		get exitValue() {
			return this.process.exitValue();
		},

		poll: function() {
			if (this._timer)
				this._timer();
		}
	};

	['onError', 'onData', 'onDone'].each(function(name) {
		var hidden = '_' + name;
		fields[name] = {
			set: function(handler) {
				this[hidden] = handler;
				updateTimer.call(this);
			},
			get: function() {
				return this[hidden];
			}
		};
	});
	return fields;
});
