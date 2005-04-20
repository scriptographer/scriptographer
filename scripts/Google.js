var dialog = new FloatingDialog(FloatingDialog.OPTION_TABBED);
dialog.setTitle("Google Image Search");

var field = new TextEdit(dialog);
field.setBounds(10, 10, 100, 20);

var button = new PushButton(dialog);
button.setText("Search");
button.setSize(button.bestSize);
button.setLocation(120, 10);
button.onClick = function() {
	search(field.text, safeList.activeEntry.text);
}

var safeText = new Static(dialog);
safeText.setText("Safe:");
safeText.setSize(safeText.bestSize);
safeText.setLocation(10, 38);

var safeList = new PopupList(dialog);
safeList.createEntry("on");
safeList.createEntry("medium");
safeList.createEntry("off").selected = true;
safeList.setBounds(safeText.bounds.maxX, 35, 80, 20);

dialog.setSize(button.bounds.maxX + 10, 80);
dialog.setVisible(true);
dialog.onClose = function() {
	this.destroy();
}

var lastQuery = "";
var step = 20;
var counter = 0;
var start = 0;

function search(query, safe) {
	if (activeDocument == null) {
		print("Please create a document first.");
	} else if (query) {

		if (query != lastQuery) {
			lastQuery  = query;
			counter = 0;
			start = 0;
		}

		var images = [];
		var mediaTracker = new java.awt.MediaTracker(new java.awt.Container());

		// the timer that checks the results of the loader thread an places the images in the document
		// we need this because illustrator is not thread safe and document cannot be modified by something
		// else than the main thread:

		var timer = new Timer(true, 30);
		timer.startTime = new Date().getTime();
		timer.images = images;
		timer.mediaTracker = mediaTracker;
		
		timer.onExecute = function() {
			if (this.images.length == 0 ||Ênew Date().getTime() - this.startTime > 1000 * 60) // timeout of 1 minute
				this.dispose();
			for (var i in this.images) {
				var img = this.images[i];
				var status = this.mediaTracker.statusID(img.id, true);
				var remove = false;
				if (status & this.mediaTracker.COMPLETE) {
					if (img.image.getWidth(null) > 0 && img.image.getHeight(null) > 0) {
						var r = new Raster(img.image);
						r.transform(Matrix.getTranslateInstance(counter * step, activeDocument.size.y - counter * step));
						counter++;
					}
					remove = true;
				} else if (status & this.mediaTracker.ABORTED || status & this.mediaTracker.ERRORED) {
					print("error: " + img.url);
					remove = true;
				}
				if (remove) {
					this.mediaTracker.removeImage(img.image, i);
					this.images.splice(i, 1);
				}
			}
		}
		
		// the loder thread:
		// first the page is requested from google, parsed and then the images are loaded.
		
		var runnable = new java.lang.Runnable() {
			images: images,
			mediaTracker: mediaTracker,
			timer: timer,
			
			run: function() {
				// safe: off, images, on
				if (safe == "medium")
					safe = "image";

				var url = new java.net.URL("http://images.google.com/images?q=" + query + "&start=" + start + "&safe=" + safe);
				start += 20;
				
				var saxReader = new Packages.org.dom4j.io.SAXReader("org.ccil.cowan.tagsoup.Parser");
				
				// fake a real http request, with User-Agent and everything, instead of:
				// var document = saxReader.read(this.url);
				
				var conn = url.openConnection();
				conn.setRequestProperty("Accept", "*/*");
				conn.setRequestProperty("Accept-Language", "en-us");
				// conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
				conn.setRequestProperty("User-Agent", "Mozilla/5.001 (Macintosh; U; PPC; en-us) Gecko/25250101");
				conn.setRequestProperty("Host", url.getHost());
				// conn.setRequestProperty("Connection", "Keep-Alive");
				
				var document = saxReader.read(conn.getInputStream());
				var list = document.selectNodes("//html:a/@href");

				var count = list.size();
				for (var i = 0; i < count; i++) {
					var href = list.get(i).text;
					var m = href.match(/(?:.*)imgurl=([^&]*)(?:\s*)/);
					if (m != null) {
						var url = new java.net.URL(m[1]);
						var img = java.awt.Toolkit.getDefaultToolkit().createImage(url);
						this.mediaTracker.addImage(img, this.images.length);
						this.images[i] = { url: url, image: img, id: i };
						// start the timer as soon as the first image is ready:
						if (!this.timer.active)
							this.timer.start();
					}
				}
			}
		}
		new java.lang.Thread(runnable).start();
	}
}