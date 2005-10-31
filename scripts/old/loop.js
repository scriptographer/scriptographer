var str = "";

var t = new Date().getTime();

for (var i = 0; i < 10000; i++) {
	str += Math.random();
}

print(new Date().getTime() - t);