function highlightAll(cls) {
	var els = document.getElementsByClassName(cls);
	for (var i = 0; i < els.length; i++) {
		hljs.highlightBlock(els[i].children[0].children[0].children[0]);
	}
}

function initHl() {
	hljs.registerLanguage("swift", swiftHighlighter);
	hljs.registerLanguage("shell", shellHighlighter);
	hljs.registerLanguage("swiftconf", swiftConfHighlighter);
	hljs.configure({languages: ["swift"]});
	highlightAll("listingblock swift");
	hljs.configure({languages: ["shell"]});
	highlightAll("listingblock shell");
	hljs.configure({languages: ["swiftconf"]});
	highlightAll("listingblock swiftconf");
}
