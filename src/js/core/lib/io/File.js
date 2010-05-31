File = Base.extend(new function() {
	var mimeTypes = null;

	return {
		beans: true,

		_type: 'file',

		/**
		 * Constructor for File objects, providing read and 
		 * write access to the file system.
		 * @class This class represents a local file or directory 
		 * @param {String|java.io.File} path as String, can be either absolute or relative
		 *		  to the helma home directory
		 * @constructor
		 */
		initialize: function(path, name) {
			if (name !== undefined) {
				this._file = new java.io.File(path, name);
			} else if (path instanceof java.io.File) {
				this._file = path;
			} else {
				this._file = new java.io.File(path);
			}
			if (!this._file.isAbsolute()) {
				// Immediately convert to absolute path - java.io.File is
				// incredibly stupid when dealing with relative file names
				// Do not use this._fil.getAbsoluteFile() as this would
				// be immediately wrapped in a js File object on Scriptographer.
				this._file = new java.io.File(this._file.getAbsolutePath());
			}
			this._eof = false;
		},

		toString: function() {
			return this._file.toString();
		},

		/**
		 * Returns the name of the file or directory represented by this File object.
		 *
		 * This is just the last name in the pathname's name sequence. 
		 * If the pathname's name sequence is empty, then the empty 
		 * string is returned.
		 * 
		 * @returns String containing the name of the file or directory
		 * @type String
		 */
		getName: function() {
			return this._file.getName() || '';
		},

		/**
		 * Returns true if the file represented by this File object
		 * is currently open.
		 * 
		 * @returns Boolean
		 * @type Boolean
		 */
		isOpened: function() {
			return !!(this._reader || this._writer);
		},

		/**
		 * Opens the file represented by this File object. If the file exists,
		 * it is used for reading, otherwise it is opened for writing.
		 * If the encoding argument is specified, it is used to read or write
		 * the file. Otherwise, the platform's default encoding is used.
		 *
		 * @param {Object} options an optional argument holder object.
		 *  The following options are supported:
		 *  <ul><li>charset name of encoding to use for reading or writing</li>
		 *  <li>append whether to append to the file if it exists</li></ul>
		 * @returns Boolean true if the operation succeeded
		 * @type Boolean
		 */
		open: function(options) {
			if (this.isOpened())
				return false;
			// We assume that the BufferedReader and PrintWriter creation
			// cannot fail except if the FileReader/FileWriter fails.
			// Otherwise we have an open file until the reader/writer
			// get garbage collected.
			var charset = options && options.charset;
			var append = options && options.append;
			if (this._file.exists() && !append) {
				if (charset) {
					this._reader = new java.io.BufferedReader(
						new java.io.InputStreamReader(
							new java.io.FileInputStream(this._file), charset));
				} else {
					this._reader = new java.io.BufferedReader(
						new java.io.FileReader(this._file));
				}
			} else if (append) {
				var stream = new java.io.FileFileOutputStream(this._file, true);
				if (charset)  {
					this._writer = new java.io.PrintWriter(
						new java.io.OutputStreamWriter(stream, charset));
				} else {
					this._writer = new java.io.PrintWriter(
						new java.io.OutputStreamWriter(stream));
				}
			} else {
				if (charset) {
					this._writer = new java.io.PrintWriter(this._file, charset);
				} else {
					this._writer = new java.io.PrintWriter(this._file);
				}
			}
			return true;
		},

		/**
		 * Tests whether the file or directory represented by this File object exists.
		 * 
		 * @returns Boolean true if the file or directory exists; false otherwise
		 * @type Boolean
		 */
		exists: function() {
			return this._file.exists();
		},

		/**
		 * Returns the pathname string of this File object's parent directory.
		 * 
		 * @returns String containing the pathname of the parent directory
		 * @type String
		 */
		getParent: function() {
			if (!this._file.getParent())
				return null;
			return new File(this._file.getParent());
		},

		/**
		 * This methods reads characters until an end of line/file is encountered 
		 * then returns the string for these characters (without any end of line 
		 * character).
		 * 
		 * @returns String of the next unread line in the file
		 * @type String
		 */
		readln: function() {
			if (!this.isOpened())
				return false;
			if (this._reader && !this._eof) {
				var line;
				if (this._lastLine != null) {
					line = this._lastLine;
					this._lastLine = null;
				} else {
					line = this._reader.readLine();
					this._eof = line == null;
				}
				return line;
			}
			return null;
		},

		/**
		 * Appends a string to the file represented by this File object.
		 * 
		 * @param {String} what as String, to be written to the file
		 * @returns Boolean
		 * @type Boolean
		 * @see #writeln
		 */
		write: function(what) {
			if (this.isOpened() && this._writer) {
				if (what != null)
					this._writer.print(what.toString());
				return true;
			}
			return false;
		},

		/**
		 * Appends a string with a platform specific end of 
		 * line to the file represented by this File object.
		 * 
		 * @param {String} what as String, to be written to the file
		 * @returns Boolean
		 * @type Boolean
		 * @see #write
		 */
		writeln: function(what) {
			if (this.write(what)) {
				this._writer.println();
				return true;
			}
			return false;
		},

		/**
		 * Tests whether this File object's pathname is absolute. 
		 *
		 * The definition of absolute pathname is system dependent. 
		 * On UNIX systems, a pathname is absolute if its prefix is "/". 
		 * On Microsoft Windows systems, a pathname is absolute if its prefix 
		 * is a drive specifier followed by "\\", or if its prefix is "\\".
		 * 
		 * @returns Boolean if this abstract pathname is absolute, false otherwise
		 * @type Boolean
		 */
		isAbsolute: function() {
			return this._file.isAbsolute();
		},

		/**
		 * List of all files within the directory represented by this File object.
		 *
		 * You may pass a RegExp Pattern to return just files matching this pattern.
		 *
		 * Example: var xmlFiles = dir.list(/.*\.xml/);
		 *
		 * @returns Array the list of File objects
		 * @type Array
		 */
		list: function(iter, recursive) {
			var files = [];
			if (!this.isOpened() && this._file.isDirectory()) {
				var regexp = Base.type(iter) == 'regexp';
				// Always use filter version, even if we're not filtering, so accept
				// can produce the File objects directly
				this._file.list(new java.io.FilenameFilter() {
					accept: function(dir, name) {
						var file = null;
					 	if (!iter || regexp && iter.test(name) || !regexp && iter(file = new File(dir, name))) {
							files.push(file || (file = new File(dir, name)));
							if (recursive && file._file.isDirectory())
								files = files.concat(file.list(iter, recursive));
						}
						// Always return false so we're not producing a java list as well
						return false;
					}
				});
			}
			return files;
		},

		/**
		 * Deletes the file or directory represented by this File object.
		 * passing true for recursive deletes directory recursively.
		 * 
		 * @returns Boolean
		 * @type Boolean
		 */
		remove: function(recursive) {
			if (recursive && this._file.isDirectory())
				for each (var file in this.list())
					file.remove(recursive);
			return this._file['delete']();
		},

		/**
		 * Purges the content of the file represented by this File object.
		 * 
		 * @returns Boolean
		 * @type Boolean
		 */
		flush: function() {
			if (this.isOpened() && this._writer) {
				this._writer.flush();
				return true;
			}
			return false;
		},

		/**
		 * Closes the file represented by this File object.
		 * 
		 * @returns Boolean
		 * @type Boolean
		 */
		close: function() {
			if (this.isOpened()) {
				if (this._reader) {
					this._reader.close();
					this._reader = null;
				}
				if (this._writer) {
					this._writer.close();
					this._writer = null;
				}
				this._eof = false;
				this._lastLine = null;
				return true;
			}
			return false;
		},

		/**
		 * Returns the pathname string of this File object. 
		 *
		 * The resulting string uses the default name-separator character 
		 * to separate the names in the name sequence.
		 * 
		 * @returns String of this file's pathname
		 * @type String
		 */
		getPath: function() {
			return this._file.getPath() || '';
		},

		/**
		 * Tests whether the application can read the file 
		 * represented by this File object.
		 * 
		 * @returns Boolean true if the file exists and can be read; false otherwise
		 * @type Boolean
		 */
		canRead: function() {
			return this._file.canRead();
		},

		/**
		 * Tests whether the file represented by this File object is writable.
		 * 
		 * @returns Boolean true if the file exists and can be modified; false otherwise.
		 * @type Boolean
		 */
		canWrite: function() {
			return this._file.canWrite();
		},

		getCanonicalFile: function() {
			return new File(this._file.getCanonicalPath());
		},

		getCanonicalPath: function() {
			return this._file.getCanonicalPath();
		},

		getAbsoluteFile: function() {
			return new File(this._file.getAbsolutePath());
		},

		/**
		 * Returns the absolute pathname string of this file.
		 *
		 * If this File object's pathname is already absolute, then the pathname 
		 * string is simply returned as if by the getPath() method. If this 
		 * abstract pathname is the empty abstract pathname then the pathname 
		 * string of the current user directory, which is named by the system 
		 * property user.dir, is returned. Otherwise this pathname is resolved 
		 * in a system-dependent way. On UNIX systems, a relative pathname is 
		 * made absolute by resolving it against the current user directory. 
		 * On Microsoft Windows systems, a relative pathname is made absolute 
		 * by resolving it against the current directory of the drive named by 
		 * the pathname, if any; if not, it is resolved against the current user 
		 * directory.
		 * 
		 * @returns String The absolute pathname string
		 * @type String
		 */
		getAbsolutePath: function() {
			return this._file.getAbsolutePath() || '';
		},

		/**
		 * Returns the size of the file represented by this File object. 
		 *
		 * The return value is unspecified if this pathname denotes a directory.
		 * 
		 * @returns Number The length, in bytes, of the file, or 0L if the file does not exist
		 * @type Number
		 */
		// Do not use getLength as a name, since this will produce the .length bean
		// and make BootStrap think it's iterable as an array.
		getSize: function() {
			return this._file.length();
		},

		getSizeAsString: function() {
			var size = this.getSize();
			if (size < 1024) return size + ' B';
			else if (size < 1048576) return Math.round(size / 10.24) / 100 + ' KB';
			else return Math.round(size / 10485.76) / 100 + ' MB';
		},

		/**
		 * Tests whether the file represented by this File object is a directory.
		 * 
		 * @returns Boolean true if this File object is a directory and exists; false otherwise
		 * @type Boolean
		 */
		isDirectory: function() {
			return this._file.isDirectory();
		},

		/**
		 * Tests whether the file represented by this File object is a normal file. 
		 *
		 * A file is normal if it is not a directory and, in addition, satisfies 
		 * other system-dependent criteria. Any non-directory file created by a 
		 * Java application is guaranteed to be a normal file.
		 * 
		 * @returns Boolean true if this File object is a normal file and exists; false otherwise
		 * @type Boolean
		 */
		isFile: function() {
			return this._file.isFile();
		},

		/**
		 * Tests whether the file represented by this File object is a hidden file.
		 *
		 * What constitutes a hidden file may depend on the platform we are running on.
		 *
		 * @returns Boolean true if this File object is hidden
		 * @type Boolean
		 */
		isHidden: function() {
			return this._file.isHidden();
		},

		/**
		 * Returns the time when the file represented by this File object was last modified.
		 *
		 * A number representing the time the file was last modified, 
		 * measured in milliseconds since the epoch (00:00:00 GMT, January 1, 1970), 
		 * or 0L if the file does not exist or if an I/O error occurs.
		 * 
		 * @returns Number in milliseconds since 00:00:00 GMT, January 1, 1970
		 * @type Number
		 */
		getLastModified: function() {
			return this._file.lastModified();
		},

		setLastModified: function(lastModified) {
			this._file.setLastModified(lastModified);
		},

		/**
		 * Creates the directory represented by this File object.
		 * 
		 * @returns Boolean true if the directory was created; false otherwise
		 * @type Boolean
		 */
		makeDirectory: function() {
			// Don't do anything if file exists or use multi directory version
			return !this.isOpened() && (this._file.isDirectory() || this._file.mkdirs());
		},

		/**
		 * Renames the file represented by this File object.
		 *
		 * Whether or not this method can move a file from one 
		 * filesystem to another is platform-dependent. The return 
		 * value should always be checked to make sure that the 
		 * rename operation was successful. 
		 * 
		 * @param {File} toFile File object containing the new path
		 * @returns true if the renaming succeeded; false otherwise
		 * @type Boolean
		 */
		renameTo: function(to) {
			return !this.isOpened() && !to.isOpened()
				&& this._file.renameTo(new java.io.File(to.getAbsolutePath()));
		},

		/**
		 * Returns true if the file represented by this File object
		 * has been read entirely and the end of file has been reached.
		 * 
		 * @returns Boolean
		 * @type Boolean
		 */
		isEof: function() {
			if (this.isOpened() && this._reader) {
				if (this._eof) {
					return true;
				} else if (this._lastLine != null) {
					return false;
				}
				this._lastLine = this._reader.readLine();
				if (this._lastLine == null)
					this._eof = true;
				return this._eof;
			}
			return true;
		},

		/**
		 * Returns the internal java file object.
		 */
		getFile: function() {
			return this._file;
		},

		/**
		 * Required by Scriptographer's framework that allows JS objects to wrap
		 * Java objects.
		 */
		unwrap: function() {
			return this._file;
		},

		/**
		 * Reads all the lines contained in the 
		 * file and returns them in an array.
		 * 
		 * @return Array of all the lines in the file
		 * @type String
		 */
		readLines: function() {
			if (!this._file.isFile())
				throw new Error("File does not exist or is not a regular file");
			var reader = new java.io.BufferedReader(new java.io.FileReader(this._file));
			// Read content line by line to setup proper eol
			var lines = [];
			while (true) {
				var line = reader.readLine();
				if (line == null)
					break;
				lines.push(line);
			}
			// Close the file
			reader.close();
			return lines;
		},

		/**
		 * Reads all the lines contained in the 
		 * file and returns them as one string.
		 * 
		 * @return String of all the lines in the file
		 * @type String
		 */
		readAll: function() {
			return this.readLines().join(java.lang.System.getProperty('line.separator'));
		},

		/**
		 * Makes a copy of a file or directory, possibly over filesystem borders.
		 * 
		 * @param {String|File} dest as a File object or the String of
		 *		  full path of the new file
		 */
		copyTo: function(file) {
			file = File.get(file);
			if (this.isDirectory()) {
				if (!file.exists() && !file.makeDirectory())
					throw new Error("Could not create directory " + file);
				var ok = true;
				for each (var f in this.list())
					ok = ok && f.copyTo(new File(file, f.getName()));
				return ok;
			} else {
				// Copy the file with FileChannels:
				file.createNewFile();
				var src = new java.io.FileInputStream(this.getPath()).getChannel();
				var dst = new java.io.FileOutputStream(file.getPath()).getChannel();
				var amount = dst.transferFrom(src, 0, src.size());
				src.close();
				dst.close();
				file.setLastModified(this.getLastModified());
				return amount > 0 || src.size() == 0;
			}
		},

		// writeToFile mimics MimePart writeToFile and uses copyTo internally
		writeToFile: function(file, filename) {
			return this.copyTo(filename ? new File(file, filename) : File.get(file));
		},

		/**
		 * Moves a file to a new destination directory.
		 * 
		 * @returns Boolean true in case file could be moved, false otherwise
		 */
		move: function(dest) {
			// instead of using the standard File method renameTo()
			// do a copyTo and then remove the source file. This way
			// file locking shouldn't be an issue
			this.copyTo(dest);
			// remove the source file
			this.remove();
			return true;
		},

		/**
		 * Returns file as ByteArray.
		 *
		 * Useful for passing it to a function instead of an request object.
		 */
		toByteArray: function() {
			if (this.exists()) {
				var body = new java.io.ByteArrayOutputStream();
				var stream = new java.io.BufferedInputStream(
					new java.io.FileInputStream(this.getAbsolutePath()));
				var buf = java.lang.reflect.Array.newInstance(java.lang.Byte.TYPE, 1024);
				var read;
				while ((read = stream.read(buf)) > -1)
					body.write(buf, 0, read);
				stream.close();
				return body.toByteArray();
			}
			return null;
		},

		/**
		 * Define iterator to loop through the lines of the file for ordinary files,
		 * or the names of contained files for directories.
		 *
		 *	for each (var line in file) ...
		 *
		 *	for each (var filename in dir) ...
		 */
		/* TODO: add support for _iterator: to bootstrap?
		__iterator__: function() {
			if (this.isDirectory()) {
				var files = this.list();
				for (var i = 0; i < files.length; i++) {
					 yield files[i];
				}
			} else if (this.exists()) {
				if (this.open()) {
					try {
						while(true) {
							yield this.readln();
						}
					} catch (e if e instanceof java.io.EOFException) {
						throw StopIteration;
					} finally {
						this.close();
					}
				}
			}
			throw StopIteration;
		},
		*/

		equals: function(object) {
			var file = null;
			if (object instanceof File) {
	            file = object._file;
	        } else if (object instanceof java.io.File) {
	            file = object;
	        } else if (object instanceof String) {
	            file = new java.io.File(object);
	        }
	        return file && file.equals(this._file);
		},

		getRelativePath: function(base) {
			var file = this;
			base = new File(base);
			var res = [];
			do {
				res.unshift(file.getName());
				file = file.getParent();
			} while (file && !file.equals(base));
			return res.join(File.separator);
		},

		getExtension: function() {
			return File.getExtension(this.getName());
		},

		getContentType: function() {
			return File.getContentType(this.getName());
		},

		createNewFile: function() {
			return this._file.createNewFile();
		},

		/**
		 * Create a new empty temporary file in the this directory, or in the directory
		 * containing this file.
		 * @param {String} prefix the prefix of the temporary file; must be at least three characters long
		 * @param {String} suffix the suffix of the temporary file; may be null
		 * @return {File} the temporary file 
		 */
		createTempFile: function(prefix, suffix) {
			return new File(java.io.File.createTempFile(prefix, suffix,
				this.isDirectory() ? this._file : this._file.getParentFile()));
		},

		statics: {
			separator: java.io.File.separator,

			get: function(file) {
				return typeof file == 'string' ? new File(file) : file;
			},

			getExtension: function(name) {
				var pos = name && name.lastIndexOf('.') || -1;
				return pos != -1 ? name.substring(pos + 1, name.length) : name;
			},

			getMimeTypes: function() {
				// Read in mime.types resource and create a lookup table.
				if (!mimeTypes) {
					// read extension to mime type mappings from mime.types:
					var resource = getResource('mime.types');
					var reader = new java.io.BufferedReader(
						new java.io.InputStreamReader(resource.getInputStream())
					);
					// Parse mime.types: 
					mimeTypes = {};
					var line;
					while ((line = reader.readLine()) != null) {
						line = line.trim();
						if (line && line[0] != '#') { // skip empty lines and comments
							// split the line at white spaces
							line = line.split(/\s+/gi);
							for (var i = 1; i < line.length; i++)
								mimeTypes[line[i]] = line[0];
						}
					}
				}
				return mimeTypes;
			},

			getContentType: function(name) {
				return File.getMimeTypes()[File.getExtension(name)] || 'application/octetstream';
			},
	
			/**
			 * Create a new empty temporary file in the default temporary-file directory.
			 * @param {String} prefix the prefix of the temporary file; must be at least three characters long
			 * @param {String} suffix the suffix of the temporary file; may be null
			 * @return {File} the temporary file
			 */
			createTempFile: function(prefix, suffix) {
				return new File(java.io.File.createTempFile(prefix, suffix));
			}
		}
	}
});
