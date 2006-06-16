function getVisible(e, def) {
	if (typeof e == "string") e = document.getElementById(e);
	if (e) {
		var d = e.style.display;
		if (d == 'none') return false;
		else if (d == '') return def;
		else return true;
	}
	return false;
}

function setVisible(e, visible) {
	if (typeof e == "string") e = document.getElementById(e);
	if (e) e.style.display = visible ? 'block' : 'none';
}

var lastMemberId = null;
function toggleMember(id, scrollTo) {
	if (lastMemberId && lastMemberId != id) {
		var prevId = lastMemberId;
		lastMemberId = null;
		toggleMember(prevId, false);
	}
	var link = document.getElementById(id + "-link");
	if (link != null) {
		var desc = document.getElementById(id + "-description");
		var v = getVisible(link, true);
		lastMemberId = v ? id : null;
		setVisible(link, !v);
		setVisible(desc, v);
		if (scrollTo)
			setTimeout("scrollToMember('" + id + "');", 1);
		return false;
	}
	return true;
}

function scrollToMember(id) {
	var e = document.getElementById(id + "-description");
	if (e && e.scrollIntoView) {
		e.scrollIntoView();
		window.scrollBy(0, -8);
	} else {
		document.location.hash = '#' + id;
	}
}

function togglePackage(id, def) {
	var e = document.getElementById("package-" + id);
	if (e) {
		var v = getVisible(e, def);
		setVisible(e, !v);
		var img = document.images["arrow-" + id];
		if (img) img.src = "../resources/arrow-" + (v ? "close" : "open") + ".gif";
	}
	return false;
}

window.onload = function()  {
	var h = document.location.hash;
	if (h) toggleMember(unescape(h.substring(1)), true);
}
